CREATE OR REPLACE FUNCTION public.run_max_emis_red_strategy(control_strategy_id integer, input_dataset_id integer, 
	input_dataset_version integer, strategy_result_id int) RETURNS void AS $$
DECLARE
	inv_table_name varchar(64) := '';
	inv_filter varchar := '';
	inv_fips_filter text := '';
	detailed_result_dataset_id integer := null;
	detailed_result_table_name varchar(64) := '';
	county_dataset_id integer := null;
	county_dataset_version integer := null;
	region RECORD;
	target_pollutant_id integer := 0;
	target_pollutant character varying(255) := '';
	measures_count integer := 0;
	measure_with_region_count integer := 0;
	measure_classes_count integer := 0;
	county_dataset_filter_sql text := '';
	cost_year integer := null;
	inventory_year integer := null;
	is_point_table boolean := false;
	use_cost_equations boolean := false;
	gimme_count integer := 0;
	min_emis_reduction_constraint real := null;
	min_control_efficiency_constraint real := null;
	max_cost_per_ton_constraint real := null;
	max_ann_cost_constraint real := null;
	replacement_control_min_eff_diff_constraint double precision := null;
	has_constraints boolean := null;
	dataset_month smallint := 0;
	no_days_in_month smallint := 31;
	ref_cost_year integer := 2006;
	cost_year_chained_gdp double precision := null;
	ref_cost_year_chained_gdp double precision := null;
	chained_gdp_adjustment_factor double precision := null;
	discount_rate double precision;
	has_design_capacity_columns boolean := false; 
	has_sic_column boolean := false; 
	has_naics_column boolean := false;
	has_target_pollutant integer := 0;
	has_latlong_columns boolean := false;
	has_plant_column boolean := false;
	get_strategt_cost_sql character varying;
	has_rpen_column boolean := false;
	get_strategt_cost_inner_sql character varying;
	annualized_uncontrolled_emis_sql character varying;
	uncontrolled_emis_sql character varying;
	emis_sql character varying;
	percent_reduction_sql character varying;
	inventory_sectors character varying := '';
	include_unspecified_costs boolean := true; 
	has_pm_target_pollutant boolean := false; 
BEGIN
--	SET work_mem TO '256MB';
--	SET enable_seqscan TO 'off';


	-- get the input dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = input_dataset_id
	into inv_table_name;

	-- get the detailed result dataset info
	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
	where sr.id = strategy_result_id
	into detailed_result_dataset_id,
		detailed_result_table_name;

	--get the inventory sector(s)
	select public.concatenate_with_ampersand(distinct name)
	from emf.sectors s
		inner join emf.datasets_sectors ds
		on ds.sector_id = s.id
	where ds.dataset_id = input_dataset_id
	into inventory_sectors;

	-- see if control strategy has only certain measures specified
	SELECT count(id), 
		count(case when region_dataset_id is not null then 1 else null end)
	FROM emf.control_strategy_measures 
	where control_strategy_measures.control_strategy_id = control_strategy_id 
	INTO measures_count, 
		measure_with_region_count;

	-- see if measure classes were specified
	IF measures_count = 0 THEN
		SELECT count(1)
		FROM emf.control_strategy_classes 
		where control_strategy_classes.control_strategy_id = control_strategy_id
		INTO measure_classes_count;
	END IF;

	-- get target pollutant, inv filter, and county dataset info if specified
	SELECT cs.pollutant_id,
		case when length(trim(cs.filter)) > 0 then '(' || public.alias_inventory_filter(cs.filter, 'inv') || ')' else null end,
		cs.cost_year,
		cs.analysis_year,
		cs.county_dataset_id,
		cs.county_dataset_version,
		cs.use_cost_equations,
		cs.discount_rate / 100,
		coalesce(cs.include_unspecified_costs,true),
		p.name
	FROM emf.control_strategies cs
		inner join emf.pollutants p
		on p.id = cs.pollutant_id
	where cs.id = control_strategy_id
	INTO target_pollutant_id,
		inv_filter,
		cost_year,
		inventory_year,
		county_dataset_id,
		county_dataset_version,
		use_cost_equations,
		discount_rate,
		include_unspecified_costs,
		target_pollutant;

	-- see if there are pm target pollutant for the stategy...
	has_pm_target_pollutant := case when target_pollutant = 'PM10' or target_pollutant = 'PM2_5' then true else false end;

	-- see if there are point specific columns in the inventory
	is_point_table := public.check_table_for_columns(inv_table_name, 'plantid,pointid,stackid,segment', ',');

	-- see if there is a sic column in the inventory
	has_sic_column := public.check_table_for_columns(inv_table_name, 'sic', ',');

	-- see if there is a naics column in the inventory
	has_naics_column := public.check_table_for_columns(inv_table_name, 'naics', ',');

	-- see if there is a rpen column in the inventory
	has_rpen_column := public.check_table_for_columns(inv_table_name, 'rpen', ',');

	-- see if there is design capacity columns in the inventory
	has_design_capacity_columns := public.check_table_for_columns(inv_table_name, 'design_capacity,design_capacity_unit_numerator,design_capacity_unit_denominator', ',');

	-- see if there is lat & long columns in the inventory
	has_latlong_columns := public.check_table_for_columns(inv_table_name, 'xloc,yloc', ',');

	-- see if there is lat & long columns in the inventory
	has_plant_column := public.check_table_for_columns(inv_table_name, 'plant', ',');

	-- get strategy constraints
	SELECT csc.max_emis_reduction,
		csc.max_control_efficiency,
		csc.min_cost_per_ton,
		csc.min_ann_cost,
		csc.replacement_control_min_eff_diff
	FROM emf.control_strategy_constraints csc
	where csc.control_strategy_id = control_strategy_id
	INTO min_emis_reduction_constraint,
		min_control_efficiency_constraint,
		max_cost_per_ton_constraint,
		max_ann_cost_constraint,
		replacement_control_min_eff_diff_constraint;

	select case when min_emis_reduction_constraint is not null 
		or min_control_efficiency_constraint is not null 
		or max_cost_per_ton_constraint is not null 
		or max_ann_cost_constraint is not null then true else false end
	into has_constraints;

	-- get month of the dataset, 0 (Zero) indicates an annual inventory
	select public.get_dataset_month(input_dataset_id)
	into dataset_month;

	IF dataset_month = 1 THEN
		no_days_in_month := 31;
	ELSIF dataset_month = 2 THEN
		no_days_in_month := 29;
	ELSIF dataset_month = 3 THEN
		no_days_in_month := 31;
	ELSIF dataset_month = 4 THEN
		no_days_in_month := 30;
	ELSIF dataset_month = 5 THEN
		no_days_in_month := 31;
	ELSIF dataset_month = 6 THEN
		no_days_in_month := 30;
	ELSIF dataset_month = 7 THEN
		no_days_in_month := 31;
	ELSIF dataset_month = 8 THEN
		no_days_in_month := 31;
	ELSIF dataset_month = 9 THEN
		no_days_in_month := 30;
	ELSIF dataset_month = 10 THEN
		no_days_in_month := 31;
	ELSIF dataset_month = 11 THEN
		no_days_in_month := 30;
	ELSIF dataset_month = 12 THEN
		no_days_in_month := 31;
	END IF;

	-- get gdp chained values
	SELECT chained_gdp
	FROM reference.gdplev
	where annual = cost_year
	INTO cost_year_chained_gdp;
	SELECT chained_gdp
	FROM reference.gdplev
	where annual = ref_cost_year
	INTO ref_cost_year_chained_gdp;

	chained_gdp_adjustment_factor := cost_year_chained_gdp / ref_cost_year_chained_gdp;

	-- if strategy have measures, then store these in a temp table for later use...
	IF measure_with_region_count > 0 THEN
		EXECUTE '
			CREATE TEMP TABLE measures (control_measure_id integer NOT NULL, region_id integer NOT NULL, region_version integer NOT NULL) ON COMMIT DROP;
			CREATE TEMP TABLE measure_regions (region_id integer NOT NULL, region_version integer NOT NULL, fips character varying(6) NOT NULL) ON COMMIT DROP;

--			CREATE TABLE measures (control_measure_id integer NOT NULL, region_id integer NOT NULL, region_version integer NOT NULL);
--			CREATE TABLE measure_regions (region_id integer NOT NULL, region_version integer NOT NULL, fips character varying(6) NOT NULL);

			CREATE INDEX measure_regions_measure_id ON measures USING btree (control_measure_id);
			CREATE INDEX measure_regions_region ON measures USING btree (region_id, region_version);
			CREATE INDEX regions_region ON measure_regions USING btree (region_id, region_version);
			CREATE INDEX regions_fips ON measure_regions USING btree (fips);';

		FOR region IN EXECUTE 
			'SELECT m.control_measure_id, i.table_name, m.region_dataset_id, m.region_dataset_version
			FROM emf.control_strategy_measures m
				inner join emf.internal_sources i
				on m.region_dataset_id = i.dataset_id
			where m.control_strategy_id = ' || control_strategy_id || '
				and m.region_dataset_id is not null'
		LOOP
			EXECUTE 'insert into measures (control_measure_id, region_id, region_version)
			SELECT ' || region.control_measure_id || ', ' || region.region_dataset_id || ', ' || region.region_dataset_version || ';';

			EXECUTE 'select count(1)
			from measure_regions
			where region_id = ' || region.region_dataset_id || '
				and region_version = ' || region.region_dataset_version || ''
			into gimme_count;

			IF gimme_count = 0 THEN
				EXECUTE 'insert into measure_regions (region_id, region_version, fips)
				SELECT ' || region.region_dataset_id || ', ' || region.region_dataset_version || ', fips
				FROM emissions.' || region.table_name || '
				where ' || public.build_version_where_filter(region.region_dataset_id, region.region_dataset_version);
			END IF;
		END LOOP;
	END IF;

	-- see if their was a county dataset specified for the strategy, is so then build a sql where clause filter for later use
	IF county_dataset_id is not null THEN
		county_dataset_filter_sql := ' and inv.fips in (SELECT fips
			FROM emissions.' || (SELECT table_name FROM emf.internal_sources where dataset_id = county_dataset_id) || '
			where ' || public.build_version_where_filter(county_dataset_id, county_dataset_version) || ')';
	END IF;
	-- build version info into where clause filter
	inv_filter := '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'inv') || ')' || coalesce(' and ' || inv_filter, '');

/*	EXECUTE '
		SELECT DISTINCT ON (poll) 1::integer as Found 
		FROM emissions.' || inv_table_name || ' as inv
		where ' || inv_filter || county_dataset_filter_sql || '
		and poll = ' || quote_literal((select name from emf.pollutants where id = target_pollutant_id)) || '
		limit 1'
	into has_target_pollutant;
	
	has_target_pollutant := coalesce(has_target_pollutant,0);

	IF has_target_pollutant = 0 THEN
		raise exception 'Target pollutant is not in the inventory';
		return;
	END IF;
*/

--	uncontrolled_emis_sql := case when dataset_month != 0 then 'coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis)' else 'inv.ann_emis' end;
--	annualized_uncontrolled_emis_sql := case when dataset_month != 0 then 'coalesce(inv.avd_emis * 365, inv.ann_emis)' else 'inv.ann_emis' end;
	uncontrolled_emis_sql := 
			case 
				when dataset_month != 0 then 
					'case when (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) / (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
				else 
					'case when (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then inv.ann_emis / (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
			end;
	emis_sql := 
			case 
				when dataset_month != 0 then 
					'coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis)' 
				else 
					'inv.ann_emis' 
			end;
	annualized_uncontrolled_emis_sql := 
			case 
				when dataset_month != 0 then 
					'case when (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.avd_emis * 365, inv.ann_emis) / (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
				else 
					'case when (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then inv.ann_emis / (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
			end;



	percent_reduction_sql := 'er.efficiency * ' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' * ' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' / 100 / 100';
	get_strategt_cost_sql := '(public.get_strategy_costs(' || case when use_cost_equations then 'true' else 'false' end || '::boolean, m.control_measures_id, 
			abbreviation, ' || discount_rate|| ', 
			m.equipment_life, er.cap_ann_ratio, 
			er.cap_rec_factor, er.ref_yr_cost_per_ton, 
			' || uncontrolled_emis_sql || ' * ' || percent_reduction_sql || ' / 100, ' || ref_cost_year_chained_gdp || ' / cast(chained_gdp as double precision), 
			' || case when use_cost_equations then 
			'et.name, 
			eq.value1, eq.value2, 
			eq.value3, eq.value4, 
			eq.value5, eq.value6, 
			eq.value7, eq.value8, 
			eq.value9, eq.value10, 
			' || case when not is_point_table then 'null' else 'inv.stkflow * 60.0' end || ', ' || case when not is_point_table then 'null' else case when not has_design_capacity_columns then 'null' else 'inv.design_capacity' end end || ', 
			' || case when not is_point_table then 'null' else case when not has_design_capacity_columns then 'null' else 'inv.design_capacity_unit_numerator' end end || ', ' || case when not is_point_table then 'null' else case when not has_design_capacity_columns then 'null' else 'inv.design_capacity_unit_denominator' end end 
			else
			'null, 
			null, null, 
			null, null, 
			null, null, 
			null, null, 
			null, null, 
			null, null, 
			null, null'
			end
			|| '))';
	get_strategt_cost_inner_sql := replace(get_strategt_cost_sql,'m.control_measures_id','m.id');


	-- add both target and cobenefit pollutants, first get best target pollutant measure, then use that to apply to other pollutants.
	execute
--	raise notice '%', 
	'insert into emissions.' || detailed_result_table_name || ' 
		(
		dataset_id,
		cm_abbrev,
		poll,
		scc,
		fips,
		' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '
		annual_oper_maint_cost,
		annualized_capital_cost,
		total_capital_cost,
		annual_cost,
		ann_cost_per_ton,
		control_eff,
		rule_pen,
		rule_eff,
		percent_reduction,
		inv_ctrl_eff,
		inv_rule_pen,
		inv_rule_eff,
		final_emissions,
		emis_reduction,
		inv_emissions,
		input_emis,
		output_emis,
		apply_order,
		fipsst,
		fipscty,
		sic,
		naics,
		source_id,
		input_ds_id,
		cs_id,
		cm_id,
		equation_type,
		sector,
		xloc,
		yloc,
		plant,
		"comment"
		)
	select DISTINCT ON (inv.fips, inv.scc, ' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || 'er.pollutant_id) 
		' || detailed_result_dataset_id || '::integer,
		abbreviation,
		inv.poll,
		inv.scc,
		inv.fips,
		' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.operation_maintenance_cost as operation_maintenance_cost,
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.annualized_capital_cost as annualized_capital_cost,
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.capital_cost as capital_cost,
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.annual_cost as ann_cost,
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.computed_cost_per_ton as computed_cost_per_ton,
		er.efficiency as efficiency,
		' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' as rule_pen,
		' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' as rule_eff,
		' || percent_reduction_sql || ' as percent_reduction,
		' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ',
		' || case when is_point_table = false then '' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || '' else '100' end || ',
		' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ',
		' || uncontrolled_emis_sql || ' * (1 - ' || percent_reduction_sql || ' / 100) as final_emissions,
		' || emis_sql || ' - ' || uncontrolled_emis_sql || ' * (1 - ' || percent_reduction_sql || ' / 100) as emis_reduction,
		' || emis_sql || ' as inv_emissions,
		' || uncontrolled_emis_sql || ' as input_emis,
		' || uncontrolled_emis_sql || ' * (1 - ' || percent_reduction_sql || ' / 100) as output_emis,
		1,
		substr(inv.fips, 1, 2),
		substr(inv.fips, 3, 3),
		' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ',
		' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ',
		inv.record_id::integer as source_id,
		' || input_dataset_id || '::integer,
		' || control_strategy_id || '::integer,
		er.control_measures_id,
		' || get_strategt_cost_sql || '.actual_equation_type as equation_type,
		' || quote_literal(inventory_sectors) || ' as sector,
		' || case when has_latlong_columns then 'inv.xloc,inv.yloc,' else 'fipscode.centerlon as xloc,fipscode.centerlat as yloc,' end || '
		' || case when has_plant_column then 'inv.plant' when not has_latlong_columns then 'fipscode.state_county_fips_code_desc as plant' else 'null::character varying as plant' end || ',
		''''
	FROM emissions.' || inv_table_name || ' inv

		inner join emf.pollutants p
		on p.name = inv.poll

		' || 
				case 
					when target_pollutant = 'PM10' or target_pollutant = 'PM2_5' then '

		left outer join emissions.' || inv_table_name || ' invpm25or10
		on invpm25or10.fips = inv.fips
		and invpm25or10.scc = inv.scc

				' || case when is_point_table then 
		'and invpm25or10.plantid = inv.plantid
		and invpm25or10.pointid = inv.pointid
		and invpm25or10.stackid = inv.stackid
		and invpm25or10.segment = inv.segment' 
				else 
					''
				end || '
		and (
			(invpm25or10.poll = ''PM2_5''
			and inv.poll = ''PM10''
			--and inv.ceff is null
			) 
		or 
			(invpm25or10.poll = ''PM10''
			and inv.poll = ''PM2_5''
			--and inv.ceff is null
			)
		)
		and (' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'invpm25or10') || ')' 
			else 
		'' 
		end || '

		' || case when not has_latlong_columns then 'left outer join reference.fips fipscode
		on fipscode.state_county_fips = inv.fips
		and fipscode.country_num = ''0''' else '' end || '

		inner join (
			-- second pass, gets rid of ties for a paticular source
			select DISTINCT ON (fips, scc' || case when is_point_table = false then '' else ', plantid, pointid, stackid, segment' end || ') 
				abbreviation,
				scc,
				fips,
				' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '
				control_measures_id,
				equipment_life
			from (
				-- get best measures for sources target pollutants, there could be a tie for a paticular source.
				select DISTINCT ON (inv.fips, inv.scc' || case when is_point_table = false then '' else ', inv.plantid, inv.pointid, inv.stackid, inv.segment' end || ', er.control_measures_id) 
					m.abbreviation,
					inv.scc,
					inv.fips,
					' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment,' end || '
					er.control_measures_id,
					' || percent_reduction_sql || ' as percent_reduction,
					' || get_strategt_cost_inner_sql || '.computed_cost_per_ton as computed_cost_per_ton,
					m.equipment_life
--,cstp.precedence

				FROM emissions.' || inv_table_name || ' inv

					inner join emf.pollutants p
					on p.name = inv.poll
					
					' || 
							case 
								when target_pollutant = 'PM10' or target_pollutant = 'PM2_5' then '

					left outer join emissions.' || inv_table_name || ' invpm25or10
					on invpm25or10.fips = inv.fips
					and invpm25or10.scc = inv.scc

							' || case when is_point_table then 
					'and invpm25or10.plantid = inv.plantid
					and invpm25or10.pointid = inv.pointid
					and invpm25or10.stackid = inv.stackid
					and invpm25or10.segment = inv.segment' 
							else 
								''
							end || '
					and (
						(invpm25or10.poll = ''PM2_5''
						and inv.poll = ''PM10''
						--and inv.ceff is null
						) 
					or 
						(invpm25or10.poll = ''PM10''
						and inv.poll = ''PM2_5''
						--and inv.ceff is null
						)
					)
					and (' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'invpm25or10') || ')' 
						else 
					'' 
					end || '

					inner join emf.control_measure_sccs scc
					on scc.name = inv.scc
					
					' || case when measures_count > 0 then '
					inner join emf.control_strategy_measures csm
					on csm.control_measure_id = scc.control_measures_id
					and csm.control_strategy_id = ' || control_strategy_id || '
					' else '' end || '

					--this part will get applicable measure based on the target pollutant, 
					-- use this measure for target and cobenefit pollutants...
					inner join emf.control_measures m
					on m.id = scc.control_measures_id

					inner join emf.control_measure_months ms
					on ms.control_measure_id = m.id
					and ms.month in (0' || case when dataset_month != 0 then ',' || dataset_month else '' end || ')

					left outer join emf.control_measure_equations eq
					on eq.control_measure_id = m.id
					and eq.pollutant_id = p.id

					left outer join emf.equation_types et
					on et.id = eq.equation_type_id

					left outer join reference.gdplev
					on gdplev.annual = eq.cost_year

					inner join emf.control_measure_efficiencyrecords er
					on er.control_measures_id = scc.control_measures_id
					-- pollutant filter
					and er.pollutant_id = p.id
					-- min and max emission filter
					and ' || annualized_uncontrolled_emis_sql || ' between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
--					and source_total.total_ann_emis between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
					-- locale filter
					and (er.locale = inv.fips or er.locale = substr(inv.fips, 1, 2) or er.locale = '''')
					-- effecive date filter
					and ' || inventory_year || '::integer >= coalesce(date_part(''year'', er.effective_date), ' || inventory_year || '::integer)		

					' || case when measures_count = 0 and measure_classes_count > 0 then '
					inner join emf.control_strategy_classes csc
					on csc.control_measure_class_id = m.cm_class_id
					and csc.control_strategy_id = ' || control_strategy_id || '
					' else '' end || '

					-- target pollutant filter
--					inner join emf.control_strategy_target_pollutants cstp
--					on cstp.pollutant_id = p.id
--					and cstp.control_strategy_id = ' || control_strategy_id || '

					' || case when include_unspecified_costs = true then '' else '
					-- include only measures that have specified costs, if no specified costs then don;t include the measure
					inner join (

						select distinct on (scc.control_measures_id, er.id, inv.fips, inv.scc
							' || case when is_point_table = false then '' else ', inv.plantid, inv.pointid, inv.stackid, inv.segment' end || ')
							scc.control_measures_id, er.id as eff_rec_id, inv.fips, inv.scc
							' || case when is_point_table = false then '' else ', inv.plantid, inv.pointid, inv.stackid, inv.segment' end || '
							--,true as valid_costs
						FROM emissions.' || inv_table_name || ' inv

							inner join emf.pollutants p
							on p.name = inv.poll
							
							' || 
									case 
										when target_pollutant = 'PM10' or target_pollutant = 'PM2_5' then '

							left outer join emissions.' || inv_table_name || ' invpm25or10
							on invpm25or10.fips = inv.fips
							and invpm25or10.scc = inv.scc

									' || case when is_point_table then 
							'and invpm25or10.plantid = inv.plantid
							and invpm25or10.pointid = inv.pointid
							and invpm25or10.stackid = inv.stackid
							and invpm25or10.segment = inv.segment' 
									else 
										''
									end || '
							and (
								(invpm25or10.poll = ''PM2_5''
								and inv.poll = ''PM10''
								and inv.ceff is null) 
							or 
								(invpm25or10.poll = ''PM10''
								and inv.poll = ''PM2_5''
								and inv.ceff is null)
							)
							and (' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'invpm25or10') || ')' 
								else 
							'' 
							end || '

							inner join emf.control_measure_sccs scc
							on scc.name = inv.scc

							' || case when measures_count > 0 then '
							inner join emf.control_strategy_measures csm
							on csm.control_measure_id = scc.control_measures_id
							and csm.control_strategy_id = ' || control_strategy_id || '
							' else '' end || '

							--this part will get applicable measure based on the target pollutant, use this measure for target and cobenefit pollutants...
							inner join emf.control_measures m
							on m.id = scc.control_measures_id

							inner join emf.control_measure_months ms
							on ms.control_measure_id = m.id
							and ms.month in (0' || case when dataset_month != 0 then ',' || dataset_month else '' end || ')

							left outer join emf.control_measure_equations eq
							on eq.control_measure_id = m.id
							and eq.pollutant_id = p.id

							left outer join emf.equation_types et
							on et.id = eq.equation_type_id

							left outer join reference.gdplev
							on gdplev.annual = eq.cost_year

							inner join emf.control_measure_efficiencyrecords er
							on er.control_measures_id = scc.control_measures_id
							-- pollutant filter
							and er.pollutant_id = p.id
							-- min and max emission filter
							and ' || annualized_uncontrolled_emis_sql || ' between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
							-- locale filter
							and (er.locale = inv.fips or er.locale = substr(inv.fips, 1, 2) or er.locale = '''')
							-- effecive date filter
							and ' || inventory_year || '::integer >= coalesce(date_part(''year'', er.effective_date), ' || inventory_year || '::integer)		

							' || case when measures_count = 0 and measure_classes_count > 0 then '
							inner join emf.control_strategy_classes csc
							on csc.control_measure_class_id = m.cm_class_id
							and csc.control_strategy_id = ' || control_strategy_id || '
							' else '' end || '


						where 	' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
							and ' || get_strategt_cost_inner_sql || '.computed_cost_per_ton is not null 
							-- dont include sources that have no emissions...
							and ' || uncontrolled_emis_sql || ' <> 0.0

--						group by scc.control_measures_id, er.id, inv.fips, inv.scc, inv.poll
--							' || case when is_point_table = false then '' else ', inv.plantid, inv.pointid, inv.stackid, inv.segment' end || '

					) tblCosts
					on tblCosts.fips = inv.fips
					and tblCosts.scc = inv.scc
--					and tblCosts.poll = inv.poll
					' || case when is_point_table = false then '' else '
					and tblCosts.plantid = inv.plantid
					and tblCosts.pointid = inv.pointid
					and tblCosts.stackid = inv.stackid
					and tblCosts.segment = inv.segment
					' end || '
					and tblCosts.control_measures_id = m.id
					and tblCosts.eff_rec_id = er.id
--					and tblCosts.valid_costs = true
					' end || '
					

				where 	' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					and p.id = ' ||  target_pollutant_id || '

					-- dont include sources that have no emissions...
					and ' || uncontrolled_emis_sql || ' <> 0.0

					-- dont include sources that have been fully controlled...
					and coalesce(100 * ' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0) <> 100.0

					-- make sure the new control is worthy, compare existing emis with new resulting emis, see if you get required percent decrease in emissions ((orig emis - new resulting emis) / orig emis * 100 ...
--					and (' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' is null or er.efficiency - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ', 0) >= ' || replacement_control_min_eff_diff_constraint || ')
					and (' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' is null or ((' ||  emis_sql || ' - ' || uncontrolled_emis_sql || ' * (1 - ' || percent_reduction_sql || ' / 100)) / ' ||  emis_sql || ' * 100 >= ' || replacement_control_min_eff_diff_constraint || '))

					-- measure region filter
					' || case when measure_with_region_count > 0 then '
					and 
					(
						csm.region_dataset_id is null 
						or
						(
							csm.region_dataset_id is not null 
							and exists (
								select 1
								from measures mr
									inner join measure_regions r
									on r.region_id = mr.region_id
									and r.region_version = mr.region_version
								where mr.control_measure_id = m.id
									and r.fips = inv.fips
							)
							and exists (
								select 1 
								from measures mr
								where mr.control_measure_id = m.id
							)
						)
					)					
					' else '' end || '
					-- constraints filter
					' || case when has_constraints then '
					and (
							' || percent_reduction_sql || ' >= ' || coalesce(min_control_efficiency_constraint, -100.0) || '
							' || coalesce(' and ' || percent_reduction_sql || ' / 100 * ' || annualized_uncontrolled_emis_sql || ' >= ' || min_emis_reduction_constraint, '')  || '
							' || coalesce(' and coalesce(' || chained_gdp_adjustment_factor || '
								* ' || get_strategt_cost_inner_sql || '.computed_cost_per_ton, -1E+308) <= ' || max_cost_per_ton_constraint, '')  || '
							' || coalesce(' and coalesce(' || percent_reduction_sql || ' / 100 * ' || annualized_uncontrolled_emis_sql || ' * ' || chained_gdp_adjustment_factor || '
								* ' || get_strategt_cost_inner_sql || '.computed_cost_per_ton, -1E+308) <= ' || max_ann_cost_constraint, '')  || '
					)' else '' end || '



				order by inv.fips, 
					inv.scc, ' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
					er.control_measures_id, 
					case when length(er.locale) = 5 then 0 when length(er.locale) = 2 then 1 else 2 end, 
					' || percent_reduction_sql || ' desc, 
					' || get_strategt_cost_inner_sql || '.computed_cost_per_ton
			) sm
			order by fips, 
				scc,' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '
--				precedence,
				percent_reduction desc, 
				computed_cost_per_ton
		) m
		on m.scc = inv.scc
		and m.fips = inv.fips
		' || case when is_point_table = false then '' else '
		and m.plantid = inv.plantid
		and m.pointid = inv.pointid
		and m.stackid = inv.stackid
		and m.segment = inv.segment' end || '

		' || case when measures_count > 0 then '
		inner join emf.control_strategy_measures csm
		on csm.control_measure_id = m.control_measures_id
		and csm.control_strategy_id = ' || control_strategy_id || '
		' else '' end || '

		left outer join emf.control_measure_equations eq
		on eq.control_measure_id = m.control_measures_id
		and eq.pollutant_id = p.id

		left outer join emf.equation_types et
		on et.id = eq.equation_type_id

		left outer join reference.gdplev
		on gdplev.annual = eq.cost_year

--					inner join (
--						select fips, scc, plantid, poll, sum(' || uncontrolled_emis_sql || ') as total_ann_emis
--						from emissions.' || inv_table_name || ' as inv
--						group by fips, scc, plantid, poll) source_total
--					on source_total.fips = inv.fips
--					and source_total.scc = inv.scc
--					and source_total.plantid = inv.plantid
--					and source_total.poll = inv.poll
					
--this part will get best eff rec...
		inner join emf.control_measure_efficiencyrecords er
		on er.control_measures_id = m.control_measures_id
		-- pollutant filter
		and er.pollutant_id = p.id
		-- min and max emission filter
		and ' || annualized_uncontrolled_emis_sql || ' between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
--		and source_total.total_ann_emis between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
		-- locale filter
		and (er.locale = inv.fips or er.locale = substr(inv.fips, 1, 2) or er.locale = '''')
		-- effecive date filter
		and ' || inventory_year || '::integer >= coalesce(date_part(''year'', er.effective_date), ' || inventory_year || '::integer)

		-- target pollutant filter
--		left outer join emf.control_strategy_target_pollutants cstp
--		on cstp.pollutant_id = p.id
--		and cstp.control_strategy_id = ' || control_strategy_id || '

	where 	' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
		-- dont include sources that have no emissions...
		and ' || uncontrolled_emis_sql || ' <> 0.0

--		and coalesce(100 * inv.ceff / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0) * ' || case when has_rpen_column then 'coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '1.0' end || ', 0) <> 100.0
		-- TODO:  I don''t think this is needed, seems redundant
--		and (	p.id <> ' ||  target_pollutant_id || '
--			or 
--			(
--				p.id = ' ||  target_pollutant_id || '
--				and er.efficiency - coalesce(inv.ceff, 0) >= ' || replacement_control_min_eff_diff_constraint || '
--			)
--		)
		-- constraint filter, BUT ONLY for target pollutants
		-- TODO:  I don''t think this is needed, seems redundant
		' || case when has_constraints then '
--		and (	p.id <> ' ||  target_pollutant_id || ' --cstp.pollutant_id is null 
--			or 
--			(
--				p.id = ' ||  target_pollutant_id || '-- cstp.pollutant_id is not null  
--				and (
--					' || percent_reduction_sql || ' >= ' || coalesce(min_control_efficiency_constraint, -100.0) || '
--					' || coalesce(' and ' || percent_reduction_sql || ' / 100 * ' || annualized_uncontrolled_emis_sql || ' >= ' || min_emis_reduction_constraint, '')  || '
--					' || coalesce(' and coalesce(' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.computed_cost_per_ton, -1E+308) <= ' || max_cost_per_ton_constraint, '')  || '
--					' || coalesce(' and coalesce(' || percent_reduction_sql || ' / 100 * ' || annualized_uncontrolled_emis_sql || ' * ' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.computed_cost_per_ton, -1E+308) <= ' || max_ann_cost_constraint, '')  || '
--				)
--			)
--		)' else '' end || '

	order by inv.fips, 
		inv.scc, 
		' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
		er.pollutant_id, 
		case when length(er.locale) = 5 then 0 when length(er.locale) = 2 then 1 else 2 end, 
		' || percent_reduction_sql || ' desc, 
		' || get_strategt_cost_sql || '.computed_cost_per_ton';

	return;
END;
$$ LANGUAGE plpgsql;
