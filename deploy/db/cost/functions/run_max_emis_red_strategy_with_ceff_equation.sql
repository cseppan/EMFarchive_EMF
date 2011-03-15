DROP FUNCTION public.run_max_emis_red_strategy(integer, integer, integer, integer);

CREATE OR REPLACE FUNCTION public.run_max_emis_red_strategy(intControlStrategyId integer, intInputDatasetId integer, 
	intInputDatasetVersion integer, intStrategyResultId int) RETURNS void AS $$
DECLARE
	strategy_name varchar(255) := '';
	inv_table_name varchar(64) := '';
	inv_filter text := '';
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
	remaining_emis_sql character varying;
	inventory_sectors character varying := '';
	include_unspecified_costs boolean := true; 
	has_pm_target_pollutant boolean := false; 
	has_cpri_column boolean := false; 
	has_primary_device_type_code_column boolean := false; 
	creator_user_id integer := 0;
	is_cost_su boolean := false; 
	get_strategty_ceff_equation_sql character varying;
BEGIN

	-- get the input dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = intInputDatasetId
	into inv_table_name;

	-- get the detailed result dataset info
	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
	where sr.id = intStrategyResultId
	into detailed_result_dataset_id,
		detailed_result_table_name;

	--get the inventory sector(s)
	select public.concatenate_with_ampersand(distinct name)
	from emf.sectors s
		inner join emf.datasets_sectors ds
		on ds.sector_id = s.id
	where ds.dataset_id = intInputDatasetId
	into inventory_sectors;

	-- see if control strategy has only certain measures specified
	SELECT count(id), 
		count(case when region_dataset_id is not null then 1 else null end)
	FROM emf.control_strategy_measures 
	where control_strategy_measures.control_strategy_id = intControlStrategyId 
	INTO measures_count, 
		measure_with_region_count;

	-- see if measure classes were specified
	IF measures_count = 0 THEN
		SELECT count(1)
		FROM emf.control_strategy_classes 
		where control_strategy_classes.control_strategy_id = intControlStrategyId
		INTO measure_classes_count;
	END IF;

	-- get target pollutant, inv filter, and county dataset info if specified
	SELECT cs.name,
		cs.pollutant_id,
		case when length(trim(cs.filter)) > 0 then '(' || public.alias_inventory_filter(cs.filter, 'inv') || ')' else null end,
		cs.cost_year,
		cs.analysis_year,
		cs.county_dataset_id,
		cs.county_dataset_version,
		cs.use_cost_equations,
		cs.discount_rate / 100,
		coalesce(cs.include_unspecified_costs,true),
		p.name,
		cs.creator_id
	FROM emf.control_strategies cs
		inner join emf.pollutants p
		on p.id = cs.pollutant_id
	where cs.id = intControlStrategyId
	INTO strategy_name,
		target_pollutant_id,
		inv_filter,
		cost_year,
		inventory_year,
		county_dataset_id,
		county_dataset_version,
		use_cost_equations,
		discount_rate,
		include_unspecified_costs,
		target_pollutant,
		creator_user_id;

	-- see if strategyt creator is a CoST SU
	SELECT 
		case 
			when 
				strpos('|' 
				|| (select p.value from emf.properties p where p.name = 'COST_SU') 
				|| '|', '|' || u.username || '|') > 0 then true 
			else false 
		end
	FROM emf.users u
	where u.id = creator_user_id
	INTO is_cost_su;

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

	-- see if there is plant column in the inventory
	has_plant_column := public.check_table_for_columns(inv_table_name, 'plant', ',');

	-- see if there is plant column in the inventory
	has_cpri_column := public.check_table_for_columns(inv_table_name, 'cpri', ',');

	-- see if there is primary_device_type_code column in the inventory
	has_primary_device_type_code_column := public.check_table_for_columns(inv_table_name, 'primary_device_type_code', ',');

	-- get strategy constraints
	SELECT csc.max_emis_reduction,
		csc.max_control_efficiency,
		csc.min_cost_per_ton,
		csc.min_ann_cost,
		csc.replacement_control_min_eff_diff
	FROM emf.control_strategy_constraints csc
	where csc.control_strategy_id = intControlStrategyId
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
	select public.get_dataset_month(intInputDatasetId)
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
/*
	EXECUTE '
		CREATE TEMP TABLE inv_overrides (record_id integer NOT NULL, nann_emis double precision, 
		navd_emis double precision,
		nceff double precision, 
		nreff double precision,
		nrpen double precision,
		cm_ids text,
		last_source_apply_order integer,
		already_controlled_pollutant_ids text) ON COMMIT DROP;';

	EXECUTE 'insert into inv_overrides (record_id, nann_emis, navd_emis, nceff, nreff, nrpen, cm_ids, last_source_apply_order, already_controlled_pollutant_ids)
		select distinct on (inv.record_id) 
			inv.record_id,
			case when dr.source_id is not null then dr.final_emissions else ann_emis end as nann_emis, 
			case when dr.source_id is not null then dr.final_emissions / 
			' ||
			case 
				when dataset_month != 0 then 
					no_days_in_month 
				else 
					'365' 
			end || ' else avd_emis end as navd_emis,
			case when dr.source_id is not null then dr.percent_reduction else ceff end as nceff, 
			case when dr.source_id is not null then 100 else reff end as nreff,
			' || case when has_rpen_column then 'case when dr.source_id is not null then 100 else rpen end as nrpen' else 'null::double precision' end || ',
			coalesce((select public.concatenate_with_pipe(CM_Id || '''') from emissions.' || detailed_result_table_name || ' dr where dr.source_id = inv.record_id),''''::text) as cm_ids,
			coalesce(
			(select max(dr_apply_order.apply_order) as last_source_apply_order 
			from emissions.' || detailed_result_table_name || ' dr_apply_order
			where dr_apply_order.fips = inv.fips
			and dr_apply_order.scc = inv.scc
			' || case when is_point_table then '
			and dr_apply_order.plantid = inv.plantid
			and dr_apply_order.pointid = inv.pointid
			and dr_apply_order.stackid = inv.stackid
			and dr_apply_order.pointid = inv.pointid
			and dr_apply_order.segment = inv.segment
			' else '
			and dr_apply_order.plantid is null
			and dr_apply_order.pointid is null
			and dr_apply_order.stackid is null
			and dr_apply_order.pointid is null
			and dr_apply_order.segment is null
			' end || ')
			, 0::integer) as last_source_apply_order,
			coalesce(
			(select public.concatenate_with_pipe(p.id || '''') as already_controlled_pollutant_ids 
			from emissions.' || detailed_result_table_name || ' dr_apply_order
				inner join emf.pollutants p
				on p.name = dr_apply_order.poll
			where dr_apply_order.fips = inv.fips
			and dr_apply_order.scc = inv.scc
			' || case when is_point_table then '
			and dr_apply_order.plantid = inv.plantid
			and dr_apply_order.pointid = inv.pointid
			and dr_apply_order.stackid = inv.stackid
			and dr_apply_order.pointid = inv.pointid
			and dr_apply_order.segment = inv.segment
			' else '
			and dr_apply_order.plantid is null
			and dr_apply_order.pointid is null
			and dr_apply_order.stackid is null
			and dr_apply_order.pointid is null
			and dr_apply_order.segment is null
			' end || ')
			,''''::text) as already_controlled_pollutant_ids


		from emissions.' || inv_table_name || ' inv

			left outer join emissions.' || detailed_result_table_name || ' dr
			on inv.record_id = dr.source_id

		where ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

		order by inv.record_id, dr.record_id desc;';
*/	



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
			where m.control_strategy_id = ' || intControlStrategyId || '
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
	inv_filter := '(' || public.build_version_where_filter(intInputDatasetId, intInputDatasetVersion, 'inv') || ')' || coalesce(' and ' || inv_filter, '');

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
	uncontrolled_emis_sql := '(' ||
			case 
				when dataset_month != 0 then 
					'case when (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) / (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
				else 
					'case when (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then inv.ann_emis / (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
			end || ')';
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
					'case when (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.avd_emis * 365, inv.ann_emis) / (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
				else 
					'case when (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then inv.ann_emis / (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
			end;


	-- build sql that calls ceff SQL equation 
	get_strategty_ceff_equation_sql := '(public.get_strategy_ceff_equation(ceff_et."value"::character varying(255), (' || emis_sql || ')::double precision, ' || case when not is_point_table then 'null::double precision, null::double precision, null::double precision' else '(inv.stkflow * 60.0)::double precision, inv.stktemp::double precision, inv.annual_avg_hours_per_year::double precision' end || ',er.efficiency, ceff_var1."value"::double precision, ceff_var2."value"::double precision))';

/*

public.get_strategy_ceff_equation(
	ceff_et.equation_type, 
	' || emis_sql || ',
	' || case when not is_point_table then 'null, null, null, null' else 'inv.stkflow * 60.0, inv.stkdiam, inv.stktemp, inv.annual_avg_hours_per_year' end || '
	er.efficiency,
	ceff_var1.var1,
	ceff_var2.var2
	)

public.get_strategy_ceff_equation(
	equation_type character varying(255), 
	ann_emis double precision, 		-- ton/yr
	stack_flow_rate double precision,	-- cfs (acfm NOT scfm) 
--	stack_diameter double precision, 	-- ft
	stack_temperature double precision, 	-- F
	annual_avg_hours_per_year double precision, -- Hrs/yr
	ceff double precision, 			-- %
	variable_coefficient1 double precision, 
	variable_coefficient2 double precision, 
	OUT computed_ceff double precision, 
	OUT actual_equation_type character varying(255)
	)
*/

	percent_reduction_sql := get_strategty_ceff_equation_sql || '.computed_ceff * ' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' * ' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' / 100 / 100';
--	percent_reduction_sql := 'er.efficiency * ' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' * ' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' / 100 / 100';
	-- relative emission reduction from inventory emission (i.e., ann emis in inv 100 tons (add on control gives addtl 50% red --> 50 tons
	-- whereas a 95% ceff replacement control on a source with an existing control of 90% reduction needs to back out source to an 
	-- uncontrolled emission 100 / (1 - 0.9) = 1000 tons giving a and then applying new control gives 950 tons reduced giving a 95% control
	remaining_emis_sql := 
		'( case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then -- add on control
			' || emis_sql || ' * ' || percent_reduction_sql || ' / 100.0 
		else -- replacement control
			(' || uncontrolled_emis_sql || ' * (1.0 - ' || percent_reduction_sql || ' / 100.0))
		end )';
/*
	remaining_emis_sql := 
		'(' || emis_sql || '
		- ( case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then -- add on control
			' || emis_sql || ' * ' || percent_reduction_sql || ' / 100.0 
		else -- replacement control
			(' || uncontrolled_emis_sql || ' * (1.0 - ' || percent_reduction_sql || ' / 100.0))
		end ))';


case when (1 - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then -- has existing control
	' || emis_sql || ' - (' || uncontrolled_emis_sql || ' * (1.0 - ' || percent_reduction_sql || ' / 100.0))
else -- has NO existing control
	' || emis_sql || ' * (1.0 - ' || percent_reduction_sql || ' / 100.0)
end
*/
	get_strategt_cost_sql := '(public.get_strategy_costs(' || case when use_cost_equations then 'true' else 'false' end || '::boolean, tpm.control_measures_id, 
			abbreviation, (' || discount_rate|| ')::double precision, 
			tpm.equipment_life::double precision, er.cap_ann_ratio::double precision, 
			er.cap_rec_factor::double precision, er.ref_yr_cost_per_ton::double precision, 
			(case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * ' || percent_reduction_sql || ' / 100)::double precision, (' || ref_cost_year_chained_gdp || ' / cast(gdplev.chained_gdp as double precision))::double precision, 
			' || case when use_cost_equations then 
			'et.name, 
			eq.value1, eq.value2, 
			eq.value3, eq.value4, 
			eq.value5, eq.value6, 
			eq.value7, eq.value8, 
			eq.value9, eq.value10, 
			' || case when not is_point_table then 'null::double precision' else '(inv.stkflow * 60.0)::double precision' end || ', ' || case when not is_point_table then 'null::double precision' else case when not has_design_capacity_columns then 'null::double precision' else 'inv.design_capacity::double precision' end end || ', 
			' || case when not is_point_table then 'null::double precision' else case when not has_design_capacity_columns then 'null::character varying' else 'inv.design_capacity_unit_numerator::character varying' end end || ', ' || case when not is_point_table then 'null::character varying' else case when not has_design_capacity_columns then 'null::character varying' else 'inv.design_capacity_unit_denominator::character varying' end end || ', ' || case when not is_point_table then 'null::double precision' else 'inv.annual_avg_hours_per_year::double precision' end 
			else
			'null::character varying(255), 
			null::double precision, null::double precision, 
			null::double precision, null::double precision, 
			null::double precision, null::double precision, 
			null::double precision, null::double precision, 
			null::double precision, null::double precision, 
			null::double precision, null::double precision, 
			null::double precision, null::double precision, 
			null::double precision'
			end
			|| ',inv.ceff::double precision, ' || ref_cost_year_chained_gdp || '::double precision / gdplev_incr.chained_gdp::double precision * er.incremental_cost_per_ton))';

	--select strpos('abc,def,ght','ght')
	get_strategt_cost_inner_sql := replace(replace(get_strategt_cost_sql,'tpm.control_measures_id','m.id'),'tpm.equipment_life','m.equipment_life');






			
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
		annual_variable_oper_maint_cost,
		annual_fixed_oper_maint_cost,
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
		"comment",
		REPLACEMENT_ADDON,
		EXISTING_MEASURE_ABBREVIATION,
		EXISTING_PRIMARY_DEVICE_TYPE_CODE,
		strategy_name,
		control_technology,
		source_group
		)
	select DISTINCT ON (inv.fips, inv.scc, ' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || 'er.pollutant_id) 
		' || detailed_result_dataset_id || '::integer,
		abbreviation,
		inv.poll,
		inv.scc,
		inv.fips,
		' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.operation_maintenance_cost as operation_maintenance_cost,
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.variable_operation_maintenance_cost as annual_variable_oper_maint_cost,
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.fixed_operation_maintenance_cost as annual_fixed_oper_maint_cost,
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.annualized_capital_cost as annualized_capital_cost,
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.capital_cost as capital_cost,
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.annual_cost as ann_cost,
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.computed_cost_per_ton as computed_cost_per_ton,
		' || get_strategty_ceff_equation_sql || '.computed_ceff as efficiency,
		' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' as rule_pen,
		' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' as rule_eff,
		' || percent_reduction_sql || ' as percent_reduction,
		' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ',
		' || case when is_point_table = false then '' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || '' else '100' end || ',
		' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ',
		case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * (1 - ' || percent_reduction_sql || ' / 100) as final_emissions,
		' || emis_sql || ' - case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * (1 - ' || percent_reduction_sql || ' / 100) as emis_reduction,
		' || emis_sql || ' as inv_emissions,
		case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end as input_emis,
		case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * (1 - ' || percent_reduction_sql || ' / 100) as output_emis,
		1,
		substr(inv.fips, 1, 2),
		substr(inv.fips, 3, 3),
		' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ',
		' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ',
		inv.record_id::integer as source_id,
		' || intInputDatasetId || '::integer,
		' || intControlStrategyId || '::integer,
		er.control_measures_id,
		' || get_strategt_cost_sql || '.actual_equation_type as equation_type,
		' || quote_literal(inventory_sectors) || ' as sector,
		' || case when has_latlong_columns then 'inv.xloc,inv.yloc,' else 'fipscode.centerlon as xloc,fipscode.centerlat as yloc,' end || '
		' || case when has_plant_column then 'inv.plant' when not has_latlong_columns then 'fipscode.state_county_fips_code_desc as plant' else 'null::character varying as plant' end || ',
		'''',
		REPLACEMENT_ADDON,
		tpm.existing_measure_abbr,
		tpm.existing_dev_code,
		' || quote_literal(strategy_name) || ' as strategy_name,
		ct.name,
		sg.name
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
		and (' || public.build_version_where_filter(intInputDatasetId, intInputDatasetVersion, 'invpm25or10') || ')' 
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
				equipment_life,
				REPLACEMENT_ADDON,
				existing_measure_abbr,
				existing_dev_code,
				control_technology,
				source_group

			from (
				-- get best measures for sources target pollutants, there could be a tie for a paticular source.
				select DISTINCT ON (inv.fips, inv.scc' || case when is_point_table = false then '' else ', inv.plantid, inv.pointid, inv.stackid, inv.segment' end || ', er.control_measures_id) 
					m.abbreviation,
					m.control_technology,
					m.source_group,
					inv.scc,
					inv.fips,
					' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment,' end || '
					er.control_measures_id,
					' || remaining_emis_sql || ' as remaining_emis,
					' || percent_reduction_sql || ' as percent_reduction,
					(select sum(' || replace(replace(replace(replace(replace(replace(replace(replace(replace(get_strategt_cost_inner_sql,'inv.','inv2.'),'er.','er2.'),'et.','et2.'),'eq.','eq2.'),'gdplev.','gdplev2.'),'gdplev_incr.','gdplev_incr2.'),'invpm25or10.','invpm25or10_2.'),'ceff_var1.','ceff_var12.'),'ceff_var2.','ceff_var22.') || '.annual_cost) as annual_cost

					FROM emissions.' || inv_table_name || ' inv2

						inner join emf.pollutants p2
						on p2.name = inv2.poll
						
						' || 
								case 
									when target_pollutant = 'PM10' or target_pollutant = 'PM2_5' then '

						left outer join emissions.' || inv_table_name || ' invpm25or10_2
						on invpm25or10_2.fips = inv2.fips
						and invpm25or10_2.scc = inv2.scc

								' || case when is_point_table then 
						'and invpm25or10_2.plantid = inv2.plantid
						and invpm25or10_2.pointid = inv2.pointid
						and invpm25or10_2.stackid = inv2.stackid
						and invpm25or10_2.segment = inv2.segment' 
								else 
									''
								end || '
						and (
							(invpm25or10_2.poll = ''PM2_5''
							and inv2.poll = ''PM10''
							--and inv2.ceff is null
							) 
						or 
							(invpm25or10_2.poll = ''PM10''
							and inv2.poll = ''PM2_5''
							--and inv2.ceff is null
							)
						)
						and (' || public.build_version_where_filter(intInputDatasetId, intInputDatasetVersion, 'invpm25or10_2') || ')' 
							else 
						'' 
						end || '

						left outer join emf.control_measure_equations eq2
						on eq2.control_measure_id = m.id
						and eq2.pollutant_id = p2.id

						left outer join emf.equation_types et2
						on et2.id = eq2.equation_type_id

						left outer join reference.gdplev gdplev2
						on gdplev2.annual = eq2.cost_year

						inner join emf.control_measure_efficiencyrecords er2
						on er2.control_measures_id = m.id
						-- pollutant filter
						and er2.pollutant_id = p2.id
						-- min and max emission filter
						and ' || replace(replace(annualized_uncontrolled_emis_sql,'inv.','inv2.'),'invpm25or10.','invpm25or10_2.') || ' between coalesce(er2.min_emis, -1E+308) and coalesce(er2.max_emis, 1E+308)
						-- locale filter
						and (er2.locale = inv2.fips or er2.locale = substr(inv2.fips, 1, 2) or er2.locale = '''')
						-- effecive date filter
						and ' || inventory_year || '::integer >= coalesce(date_part(''year'', er2.effective_date), ' || inventory_year || '::integer)		

						-- Replacement vs Add On Logic...
						and (

							-- Measure is Add On Only!
							(
								(coalesce(er2.existing_measure_abbr, '''') <> '''' or er2.existing_dev_code <> 0)
								and 
								(
									(length(inv2.control_measures) > 0 and strpos(''&'' || coalesce(inv2.control_measures, '''') || ''&'', ''&'' || er2.existing_measure_abbr || ''&'') > 0) '
									|| case when has_cpri_column then ' or (inv2.cpri <> 0 and er2.existing_dev_code = inv2.cpri) '
									when has_primary_device_type_code_column then ' or (length(inv2.primary_device_type_code) > 0 and er2.existing_dev_code || '''' = inv2.primary_device_type_code) '
									else '' end || '
								)
							)

							-- Measure is Replacement Only!
							or 
							(
								coalesce(er2.existing_measure_abbr, '''') = '''' and coalesce(er2.existing_dev_code, 0) = 0
							)


						)
		
						left outer join reference.gdplev gdplev_incr2
						on gdplev_incr2.annual = er2.cost_year

					left outer join emf.control_measure_properties ceff_et2
					on ceff_et2.control_measure_id = m.id
					and ceff_et2."name" = ''CEFF_EQUATION_'' || inv2.poll || ''_TYPE''

					left outer join emf.control_measure_properties ceff_var12
					on ceff_var12.control_measure_id = m.id
					and ceff_var12."name" = ''CEFF_EQUATION_'' || inv2.poll || ''_VAR1''

					left outer join emf.control_measure_properties ceff_var22
					on ceff_var22.control_measure_id = m.id
					and ceff_var22."name" = ''CEFF_EQUATION_'' || inv2.poll || ''_VAR2''

					where 	' || replace(inv_filter,'inv.','inv2.') || coalesce(replace(county_dataset_filter_sql,'inv.','inv2.'), '') || '

						-- limit to specific source
						and inv2.fips = inv.fips
						and inv2.scc = inv.scc
						' || case when is_point_table = false then '' else '
						and inv2.plantid = inv.plantid
						and inv2.pointid = inv.pointid
						and inv2.stackid = inv.stackid
						and inv2.segment = inv.segment
						' end || '

						-- dont include sources that have no emissions...
						and ' || replace(replace(uncontrolled_emis_sql,'inv.','inv2.'),'invpm25or10.','invpm25or10_2.') || ' <> 0.0

						-- dont include sources that have been fully controlled...
						and coalesce(100 * ' || case when not has_pm_target_pollutant then 'inv2.ceff' else 'coalesce(inv2.ceff, invpm25or10_2.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv2.reff' else 'coalesce(inv2.reff, invpm25or10_2.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv2.rpen' else 'coalesce(inv2.rpen, invpm25or10_2.rpen)' end || ' / 100, 1.0)' else '' end || ', 0) <> 100.0

					) as source_annual_cost,
					m.equipment_life,
					case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ''A''
					else ''R'' end as REPLACEMENT_ADDON,
					er.existing_measure_abbr,
					er.existing_dev_code

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
					and (' || public.build_version_where_filter(intInputDatasetId, intInputDatasetVersion, 'invpm25or10') || ')' 
						else 
					'' 
					end || '

					inner join emf.control_measure_sccs scc
					on scc.name = inv.scc
					
					' || case when measures_count > 0 then '
					inner join emf.control_strategy_measures csm
					on csm.control_measure_id = scc.control_measures_id
					and csm.control_strategy_id = ' || intControlStrategyId || '
					' else '' end || '

					--this part will get applicable measure based on the target pollutant, 
					-- use this measure for target and cobenefit pollutants...
					inner join emf.control_measures m
					on m.id = scc.control_measures_id

-- for Non CoST SUs, make sure they only see their temporary measures
					' || case when not is_cost_su then '

					inner join emf.control_measure_classes cmc
					on cmc.id = m.cm_class_id
					and (
						(cmc.name = ''Temporary'' and m.creator = ' || creator_user_id || ')
						or (cmc.name <> ''Temporary'')
					)
					' else '' end || '

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
					-- Replacement vs Add On Logic...
					and (

						-- Measure is Add On Only!
						(
							(coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0)
							and 
							(
								(length(inv.control_measures) > 0 and strpos(''&'' || coalesce(inv.control_measures, '''') || ''&'', ''&'' || er.existing_measure_abbr || ''&'') > 0) '
								|| case when has_cpri_column then ' or (inv.cpri <> 0 and er.existing_dev_code = inv.cpri) '
								when has_primary_device_type_code_column then ' or (length(inv.primary_device_type_code) > 0 and er.existing_dev_code || '''' = inv.primary_device_type_code) '
								else '' end || '
							)
						)

						-- Measure is Replacement Only!
						or 
						(
							coalesce(er.existing_measure_abbr, '''') = '''' and coalesce(er.existing_dev_code, 0) = 0
						)


					)


					left outer join reference.gdplev gdplev_incr
					on gdplev_incr.annual = er.cost_year

					' || case when measures_count = 0 and measure_classes_count > 0 then '
					inner join emf.control_strategy_classes csc
					on csc.control_measure_class_id = m.cm_class_id
					and csc.control_strategy_id = ' || intControlStrategyId || '
					' else '' end || '

					-- target pollutant filter
--					inner join emf.control_strategy_target_pollutants cstp
--					on cstp.pollutant_id = p.id
--					and cstp.control_strategy_id = ' || intControlStrategyId || '

					left outer join emf.control_measure_properties ceff_et
					on ceff_et.control_measure_id = m.id
					and ceff_et."name" = ''CEFF_EQUATION_'' || inv.poll || ''_TYPE''

					left outer join emf.control_measure_properties ceff_var1
					on ceff_var1.control_measure_id = m.id
					and ceff_var1."name" = ''CEFF_EQUATION_'' || inv.poll || ''_VAR1''

					left outer join emf.control_measure_properties ceff_var2
					on ceff_var2.control_measure_id = m.id
					and ceff_var2."name" = ''CEFF_EQUATION_'' || inv.poll || ''_VAR2''

				where 	' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					and p.id = ' ||  target_pollutant_id || '

					-- dont include sources that have no emissions...
					and ' || uncontrolled_emis_sql || ' <> 0.0

					-- dont include sources that have been fully controlled...
					and coalesce(100 * ' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv.reff' else 'coalesce(inv.reff, invpm25or10.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv.rpen' else 'coalesce(inv.rpen, invpm25or10.rpen)' end || ' / 100, 1.0)' else '' end || ', 0) <> 100.0

					-- make sure the new control is worthy, compare existing emis with new resulting emis, see if you get required percent decrease in emissions ((orig emis - new resulting emis) / orig emis * 100 ...
--					and (' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' is null or er.efficiency - coalesce(' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ', 0) >= ' || replacement_control_min_eff_diff_constraint || ')
--					and (' || case when not has_pm_target_pollutant then 'inv.ceff' else 'coalesce(inv.ceff, invpm25or10.ceff)' end || ' is null or ((' ||  emis_sql || ' - case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end  * (1 - ' || percent_reduction_sql || ' / 100)) / ' ||  emis_sql || ' * 100 >= ' || replacement_control_min_eff_diff_constraint || '))


					-- make sure the new control is worthy
					-- this is only relevant for Replacement controls, not Add-on controls or sources with no existing control
					and (
						-- source has no existing control
						(
							' || case when not has_pm_target_pollutant then 'coalesce(inv.ceff, 0.0)' else 'coalesce(inv.ceff, invpm25or10.ceff, 0.0)' end || ' = 0.0
						)
						-- control is add-on type
						or (
							(coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0)
						)
						-- replacement control that meets the constraint, source has existing control
						or (
							' || case when not has_pm_target_pollutant then 'coalesce(inv.ceff, 0.0)' else 'coalesce(inv.ceff, invpm25or10.ceff, 0.0)' end || ' <> 0.0
							and (coalesce(er.existing_measure_abbr, '''') = '''' and coalesce(er.existing_dev_code, 0) = 0)
							and ((' ||  emis_sql || ') - (' || remaining_emis_sql || ')) / (' ||  emis_sql || ') * 100 >= ' || replacement_control_min_eff_diff_constraint || '
						)
					)

					-- dont include measures already on the source...
					and strpos(''&'' || coalesce(inv.control_measures, '''') || ''&'', ''&'' || m.abbreviation || ''&'') = 0

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
							' || coalesce(' and (' ||  emis_sql || ') - (' || remaining_emis_sql || ') >= ' || min_emis_reduction_constraint, '')  || '
							' || coalesce(' and coalesce(' || chained_gdp_adjustment_factor || '
								* ' || get_strategt_cost_inner_sql || '.computed_cost_per_ton, -1E+308) <= ' || max_cost_per_ton_constraint, '')  || '
							' || coalesce(' and coalesce(' || percent_reduction_sql || ' / 100 * ' || annualized_uncontrolled_emis_sql || ' * ' || chained_gdp_adjustment_factor || '
								* ' || get_strategt_cost_inner_sql || '.computed_cost_per_ton, -1E+308) <= ' || max_ann_cost_constraint, '')  || '
					)' else '' end || '

					' || case when include_unspecified_costs = true then '' else '
					and (select sum(' || replace(replace(replace(replace(replace(replace(replace(get_strategt_cost_inner_sql,'inv.','inv2.'),'er.','er2.'),'et.','et2.'),'eq.','eq2.'),'gdplev.','gdplev2.'),'gdplev_incr.','gdplev_incr2.'),'invpm25or10.','invpm25or10_2.') || '.annual_cost) as annual_cost

					FROM emissions.' || inv_table_name || ' inv2

						inner join emf.pollutants p2
						on p2.name = inv2.poll
						
						' || 
								case 
									when target_pollutant = 'PM10' or target_pollutant = 'PM2_5' then '

						left outer join emissions.' || inv_table_name || ' invpm25or10_2
						on invpm25or10_2.fips = inv2.fips
						and invpm25or10_2.scc = inv2.scc

								' || case when is_point_table then 
						'and invpm25or10_2.plantid = inv2.plantid
						and invpm25or10_2.pointid = inv2.pointid
						and invpm25or10_2.stackid = inv2.stackid
						and invpm25or10_2.segment = inv2.segment' 
								else 
									''
								end || '
						and (
							(invpm25or10_2.poll = ''PM2_5''
							and inv2.poll = ''PM10''
							--and inv2.ceff is null
							) 
						or 
							(invpm25or10_2.poll = ''PM10''
							and inv2.poll = ''PM2_5''
							--and inv2.ceff is null
							)
						)
						and (' || public.build_version_where_filter(intInputDatasetId, intInputDatasetVersion, 'invpm25or10_2') || ')' 
							else 
						'' 
						end || '

						left outer join emf.control_measure_equations eq2
						on eq2.control_measure_id = m.id
						and eq2.pollutant_id = p2.id

						left outer join emf.equation_types et2
						on et2.id = eq2.equation_type_id

						left outer join reference.gdplev gdplev2
						on gdplev2.annual = eq2.cost_year

						inner join emf.control_measure_efficiencyrecords er2
						on er2.control_measures_id = m.id
						-- pollutant filter
						and er2.pollutant_id = p2.id
						-- min and max emission filter
						and ' || replace(replace(annualized_uncontrolled_emis_sql,'inv.','inv2.'),'invpm25or10.','invpm25or10_2.') || ' between coalesce(er2.min_emis, -1E+308) and coalesce(er2.max_emis, 1E+308)
						-- locale filter
						and (er2.locale = inv2.fips or er2.locale = substr(inv2.fips, 1, 2) or er2.locale = '''')
						-- effecive date filter
						and ' || inventory_year || '::integer >= coalesce(date_part(''year'', er2.effective_date), ' || inventory_year || '::integer)		
						-- Replacement vs Add On Logic...
						and (

							-- Measure is Add On Only!
							(
								(coalesce(er2.existing_measure_abbr, '''') <> '''' or er2.existing_dev_code <> 0)
								and 
								(
									(length(inv2.control_measures) > 0 and strpos(''&'' || coalesce(inv2.control_measures, '''') || ''&'', ''&'' || er2.existing_measure_abbr || ''&'') > 0) '
									|| case when has_cpri_column then ' or (inv2.cpri <> 0 and er2.existing_dev_code = inv2.cpri) '
								when has_primary_device_type_code_column then ' or (length(inv2.primary_device_type_code) > 0 and er2.existing_dev_code || '''' = inv2.primary_device_type_code) '
								else '' end || '
								)
							)

							-- Measure is Replacement Only!
							or 
							(
								coalesce(er2.existing_measure_abbr, '''') = '''' and coalesce(er2.existing_dev_code, 0) = 0
							)

						)

						left outer join reference.gdplev gdplev_incr2
						on gdplev_incr2.annual = er2.cost_year

						left outer join emf.control_measure_properties ceff_et
						on ceff_et.control_measure_id = m.id
						and ceff_et."name" = ''CEFF_EQUATION_'' || inv2.poll || ''_TYPE''

						left outer join emf.control_measure_properties ceff_var1
						on ceff_var1.control_measure_id = m.id
						and ceff_var1."name" = ''CEFF_EQUATION_'' || inv2.poll || ''_VAR1''

						left outer join emf.control_measure_properties ceff_var2
						on ceff_var2.control_measure_id = m.id
						and ceff_var2."name" = ''CEFF_EQUATION_'' || inv2.poll || ''_VAR2''

					where 	' || replace(inv_filter,'inv.','inv2.') || coalesce(replace(county_dataset_filter_sql,'inv.','inv2.'), '') || '

						-- limit to specific source
						and inv2.fips = inv.fips
						and inv2.scc = inv.scc
						' || case when is_point_table = false then '' else '
						and inv2.plantid = inv.plantid
						and inv2.pointid = inv.pointid
						and inv2.stackid = inv.stackid
						and inv2.segment = inv.segment
						' end || '

						-- dont include sources that have no emissions...
						and ' || replace(replace(uncontrolled_emis_sql,'inv.','inv2.'),'invpm25or10.','invpm25or10_2.') || ' <> 0.0

						-- dont include sources that have been fully controlled...
						and coalesce(100 * ' || case when not has_pm_target_pollutant then 'inv2.ceff' else 'coalesce(inv2.ceff, invpm25or10_2.ceff)' end || ' / 100 * coalesce(' || case when not has_pm_target_pollutant then 'inv2.reff' else 'coalesce(inv2.reff, invpm25or10_2.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not has_pm_target_pollutant then 'inv2.rpen' else 'coalesce(inv2.rpen, invpm25or10_2.rpen)' end || ' / 100, 1.0)' else '' end || ', 0) <> 100.0

					) > 0.0
					' end || '

				order by inv.fips, 
					inv.scc, ' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
					er.control_measures_id, 
					case when length(er.locale) = 5 then 0 when length(er.locale) = 2 then 1 else 2 end, 
					' || remaining_emis_sql || '
			) sm
			order by fips, 
				scc,' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '
				remaining_emis,
				coalesce(source_annual_cost,0.0)
		) tpm
		on tpm.scc = inv.scc
		and tpm.fips = inv.fips
		' || case when is_point_table = false then '' else '
		and tpm.plantid = inv.plantid
		and tpm.pointid = inv.pointid
		and tpm.stackid = inv.stackid
		and tpm.segment = inv.segment' end || '

		' || case when measures_count > 0 then '
		inner join emf.control_strategy_measures csm
		on csm.control_measure_id = tpm.control_measures_id
		and csm.control_strategy_id = ' || intControlStrategyId || '
		' else '' end || '

		left outer join emf.control_measure_equations eq
		on eq.control_measure_id = tpm.control_measures_id
		and eq.pollutant_id = p.id

		left outer join emf.equation_types et
		on et.id = eq.equation_type_id

		left outer join reference.gdplev
		on gdplev.annual = eq.cost_year

		left outer join emf.control_technologies ct
		on ct.id = tpm.control_technology

		left outer join emf.source_groups sg
		on sg.id = tpm.source_group

		--this part will get best eff rec...
		inner join emf.control_measure_efficiencyrecords er
		on er.control_measures_id = tpm.control_measures_id
		-- pollutant filter
		and er.pollutant_id = p.id
		-- min and max emission filter
		and ' || annualized_uncontrolled_emis_sql || ' between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
		-- locale filter
		and (er.locale = inv.fips or er.locale = substr(inv.fips, 1, 2) or er.locale = '''')
		-- effecive date filter
		and ' || inventory_year || '::integer >= coalesce(date_part(''year'', er.effective_date), ' || inventory_year || '::integer)

		-- Replacement vs Add On Logic...
		and (

			-- Measure is Add On Only!
			(
				tpm.REPLACEMENT_ADDON = ''A''
				and 
				(tpm.existing_measure_abbr = er.existing_measure_abbr
				or tpm.existing_dev_code = er.existing_dev_code)
			)

			-- Measure is Replacement Only!
			or 
			(
				tpm.REPLACEMENT_ADDON = ''R''
			)

		)

		left outer join reference.gdplev gdplev_incr
		on gdplev_incr.annual = er.cost_year

		left outer join emf.control_measure_properties ceff_et
		on ceff_et.control_measure_id = tpm.control_measures_id
		and ceff_et."name" = ''CEFF_EQUATION_'' || inv.poll || ''_TYPE''

		left outer join emf.control_measure_properties ceff_var1
		on ceff_var1.control_measure_id = tpm.control_measures_id
		and ceff_var1."name" = ''CEFF_EQUATION_'' || inv.poll || ''_VAR1''

		left outer join emf.control_measure_properties ceff_var2
		on ceff_var2.control_measure_id = tpm.control_measures_id
		and ceff_var2."name" = ''CEFF_EQUATION_'' || inv.poll || ''_VAR2''

	where 	' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
		-- dont include sources that have no emissions...
		and ' || uncontrolled_emis_sql || ' <> 0.0

	order by inv.fips, 
		inv.scc, 
		' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
		er.pollutant_id, 
		case when length(er.locale) = 5 then 0 when length(er.locale) = 2 then 1 else 2 end, 
		' || remaining_emis_sql || ', 
--		tpm.source_annual_cost
		' || get_strategt_cost_sql || '.computed_cost_per_ton';

	return;






	-- add target pollutant records
	execute 'insert into strat_worksheet (
	--		Dataset_Id,
			cm_abbrev,
			poll,
			scc,
			fips,
			' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '
			annual_oper_maint_cost,
			annual_variable_oper_maint_cost,
			annual_fixed_oper_maint_cost,
			annualized_capital_cost,
			total_capital_cost,
			annual_cost,
			ann_cost_per_ton,
			control_eff,
			rule_pen,
			rule_eff,
			percent_reduction,
			final_emissions,
			Inv_Ctrl_Eff,
			Inv_Rule_Pen,
			Inv_Rule_Eff,
			emis_reduction,
			inv_emissions,
			input_emis,
			output_emis,
			sic,
			naics,
			source_id,
			input_ds_id,
			cm_id,
			equation_type,
			original_dataset_id,
			sector,
			source,
			xloc,
			yloc,
			plant,
			REPLACEMENT_ADDON,
			EXISTING_MEASURE_ABBREVIATION,
			EXISTING_PRIMARY_DEVICE_TYPE_CODE
			) 
	select *
	from (
		select DISTINCT ON (inv.record_id, er.control_measures_id) 
			m.abbreviation,
			inv.poll,
			inv.scc,
			inv.fips,
			' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
			' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.operation_maintenance_cost as operation_maintenance_cost,
			' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.variable_operation_maintenance_cost as annual_variable_oper_maint_cost,
			' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.fixed_operation_maintenance_cost as annual_fixed_oper_maint_cost,
			' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.annualized_capital_cost as annualized_capital_cost,
			' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.capital_cost as capital_cost,
			' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.annual_cost as annual_cost,
			' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.computed_cost_per_ton as computed_cost_per_ton,
			efficiency,
			' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' as rule_pen,
			' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' as rule_eff,
			' || percent_reduction_sql || ' as percent_reduction,
			case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * (1 - ' || percent_reduction_sql || ' / 100) as final_emissions,
			inv.ceff,
			' || case when has_rpen_column then 'coalesce(inv.rpen, 100.0::double precision)' else '100.0::double precision' end || ',
			coalesce(inv.reff, 100.0::double precision),
			' || emis_sql || ' - case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * (1 - ' || percent_reduction_sql || ' / 100) as emis_reduction,
			' || emis_sql || ' as inv_emissions,
			case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end as input_emis,
			case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * (1 - ' || percent_reduction_sql || ' / 100) as output_emis,
			' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ',
			' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ',
			inv.record_id::integer as source_id,
			' || intInputDatasetId || '::integer as input_ds_id,
			er.control_measures_id as cm_id,
			' || get_strategt_cost_inner_sql || '.actual_equation_type as equation_type,
			null::integer as original_dataset_id, ' || quote_literal('sector') || '::text as sector
			,
			sources.id as source,

/*(select sources.id from emf.sources where sources.scc = inv.scc
			and sources.fips = inv.fips
			' || case when is_point_table = false then '
			and sources.plantid is null
			and sources.pointid is null
			and sources.stackid is null
			and sources.segment is null
			' else '
			and sources.plantid = inv.plantid
			and sources.pointid = inv.pointid
			and sources.stackid = inv.stackid
			and sources.segment = inv.segment
			' end || ') as source
*/
			' || case when has_latlong_columns then 'inv.xloc,inv.yloc,' else 'fipscode.centerlon as xloc,fipscode.centerlat as yloc,' end || '
			' || case when has_plant_column then 'inv.plant' when not has_latlong_columns then 'fipscode.state_county_fips_code_desc as plant' else 'null::character varying as plant' end || ',
			case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ''A''
			else ''R'' end as REPLACEMENT_ADDON,
			er.existing_measure_abbr as EXISTING_MEASURE_ABBREVIATION,
			er.existing_dev_code as EXISTING_PRIMARY_DEVICE_TYPE_CODE
		FROM emissions.' || inv_table_name || ' inv
			' || case when not has_latlong_columns then 'left outer join reference.fips fipscode
			on fipscode.state_county_fips = inv.fips
			and fipscode.country_num = ''0''' else '' end || '

			inner join emf.sources

			on sources.source = inv.scc || inv.fips || ' || case when is_point_table = false then 'repeat('' '', 60) ' else 'rpad(coalesce(inv.plantid, ''''), 15) || rpad(coalesce(inv.pointid, ''''), 15) || rpad(coalesce(inv.stackid, ''''), 15) || rpad(coalesce(inv.segment, ''''), 15)' end || '
/*			on sources.scc = inv.scc
			and sources.fips = inv.fips
			' || case when is_point_table = false then '
			and sources.plantid is null
			and sources.pointid is null
			and sources.stackid is null
			and sources.segment is null
			' else '
			and sources.plantid = inv.plantid
			and sources.pointid = inv.pointid
			and sources.stackid = inv.stackid
			and sources.segment = inv.segment
			' end || '
*/
			inner join emf.control_measure_sccs scc
			on scc.name = inv.scc

			inner join emf.control_measures m
			on m.id = scc.control_measures_id

			' || case when measures_count > 0 then '
			inner join emf.control_strategy_measures csm
			on csm.control_measure_id = m.id
			and csm.control_strategy_id = ' || intControlStrategyId || '::integer
			' else '' end || '

			' || case when measures_count = 0 and measure_classes_count > 0 then '
			inner join emf.control_strategy_classes csc
			on csc.control_measure_class_id = m.cm_class_id
			and csc.control_strategy_id = ' || intControlStrategyId || '::integer
			' else '' end || '

			inner join emf.pollutants p
			on p.name = inv.poll

			inner join emf.control_measure_efficiencyrecords er
			on er.control_measures_id = m.id
			-- pollutant filter
			and er.pollutant_id = p.id
			-- min and max emission filter
			and ' || annualized_uncontrolled_emis_sql || ' between coalesce(er.min_emis, -1E+308::double precision) and coalesce(er.max_emis, 1E+308::double precision)
			-- locale filter
			and (er.locale = inv.fips or er.locale = substr(inv.fips, 1, 2) or er.locale = ''''::varchar(6))
			-- effecive date filter
			and ' || inventory_year || '::integer >= coalesce(date_part(''year'', er.effective_date)::integer, ' || inventory_year || '::integer)
			-- Replacement vs Add On Logic...
			and (

				-- Measure is Add On Only!
				(
					(coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0)
					and 
					(
						(length(inv.control_measures) > 0 and strpos('','' || coalesce(inv.control_measures, '''') || '','', '','' || er.existing_measure_abbr || '','') > 0) '
						|| case when has_cpri_column then ' or (inv.cpri <> 0 and er.existing_dev_code = inv.cpri) '
						when has_primary_device_type_code_column then ' or (length(inv.primary_device_type_code) > 0 and er.existing_dev_code || '''' = inv.primary_device_type_code) '
						else '' end || '
					)
				)

				-- Measure is Replacement Only!
				or 
				(
					coalesce(er.existing_measure_abbr, '''') = '''' and coalesce(er.existing_dev_code, 0) = 0
				)


			)

			left outer join reference.gdplev gdplev_incr
			on gdplev_incr.annual = er.cost_year

-- for Non CoST SUs, make sure they only see their temporary measures
			' || case when not is_cost_su then '

			inner join emf.control_measure_classes cmc
			on cmc.id = m.cm_class_id
			and (
				(cmc.name = ''Temporary'' and m.creator = ' || creator_user_id || ')
				or (cmc.name <> ''Temporary'')
			)
			' else '' end || '

			inner join emf.control_measure_months ms
			on ms.control_measure_id = m.id
			and ms.month in (0::integer' || case when dataset_month != 0 then ',' || dataset_month || '::integer' else '' end || ')

			left outer join emf.control_measure_equations eq
			on eq.control_measure_id = m.id
			and eq.pollutant_id = p.id

			left outer join emf.equation_types et
			on et.id = eq.equation_type_id

			left outer join reference.gdplev
			on gdplev.annual = eq.cost_year

					left outer join emf.control_measure_properties ceff_et
					on ceff_et.control_measure_id = m.id
					and ceff_et."name" = ''CEFF_EQUATION_'' || inv.poll || ''_TYPE''

					left outer join emf.control_measure_properties ceff_var1
					on ceff_var1.control_measure_id = m.id
					and ceff_var1."name" = ''CEFF_EQUATION_'' || inv.poll || ''_VAR1''

					left outer join emf.control_measure_properties ceff_var2
					on ceff_var2.control_measure_id = m.id
					and ceff_var2."name" = ''CEFF_EQUATION_'' || inv.poll || ''_VAR2''

					left outer join public.get_strategy_costs(' || case when use_cost_equations then 'true' else 'false' end || '::boolean, m.id, 
			abbreviation, (' || discount_rate|| ')::double precision, 
			m.equipment_life::double precision, er.cap_ann_ratio::double precision, 
			er.cap_rec_factor::double precision, er.ref_yr_cost_per_ton::double precision, 
			(case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * ' || percent_reduction_sql || ' / 100)::double precision, (' || ref_cost_year_chained_gdp || ' / cast(gdplev.chained_gdp as double precision))::double precision, 
			' || case when use_cost_equations then 
			'et.name, 
			eq.value1, eq.value2, 
			eq.value3, eq.value4, 
			eq.value5, eq.value6, 
			eq.value7, eq.value8, 
			eq.value9, eq.value10, 
			' || case when not is_point_table then 'null::double precision' else '(inv.stkflow * 60.0)::double precision' end || ', ' || case when not is_point_table then 'null::double precision' else case when not has_design_capacity_columns then 'null::double precision' else 'inv.design_capacity::double precision' end end || ', 
			' || case when not is_point_table then 'null::double precision' else case when not has_design_capacity_columns then 'null::character varying' else 'inv.design_capacity_unit_numerator::character varying' end end || ', ' || case when not is_point_table then 'null::character varying' else case when not has_design_capacity_columns then 'null::character varying' else 'inv.design_capacity_unit_denominator::character varying' end end || ', ' || case when not is_point_table then 'null::double precision' else 'inv.annual_avg_hours_per_year::double precision' end 
			else
			'null::character varying(255), 
			null::double precision, null::double precision, 
			null::double precision, null::double precision, 
			null::double precision, null::double precision, 
			null::double precision, null::double precision, 
			null::double precision, null::double precision, 
			null::double precision, null::double precision, 
			null::double precision, null::double precision, 
			null::double precision'
			end
			|| ',inv.ceff::double precision, ' || ref_cost_year_chained_gdp || '::double precision / gdplev_incr.chained_gdp::double precision * er.incremental_cost_per_ton) as costs

		where 	' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
			and p.id = ' || target_pollutant_id || '::integer

			-- dont include sources that have no emissions...
			and ' || uncontrolled_emis_sql || ' <> 0.0

			-- dont include sources that have been fully controlled...
			and coalesce(100 * inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0) <> 100.0

			-- make sure the new control is worthy
			-- this is only relevant for Replacement controls, not Add-on controls or sources with no existing control
			and (
				-- source has no existing control
				(
					coalesce(inv.ceff, 0.0) = 0.0
				)
				-- control is add-on type
				or (
					(coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0)
				)
				-- replacement control that meets the constraint, source has existing control
				or (
					coalesce(inv.ceff, 0.0) <> 0.0
					and (coalesce(er.existing_measure_abbr, '''') = '''' and coalesce(er.existing_dev_code, 0) = 0)
					and ((' ||  emis_sql || ') - (' || remaining_emis_sql || ')) / (' ||  emis_sql || ') * 100 >= ' || replacement_control_min_eff_diff_constraint || '
				)
			)

			-- dont include measures already on the source...
			and strpos(''&'' || coalesce(inv.control_measures, '''') || ''&'', ''&'' || m.abbreviation || ''&'') = 0

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
					' || coalesce(' and (' ||  emis_sql || ') - (' || remaining_emis_sql || ') >= ' || min_emis_reduction_constraint, '')  || '
					' || coalesce(' and coalesce(' || chained_gdp_adjustment_factor || '
						* ' || get_strategt_cost_inner_sql || '.computed_cost_per_ton, -1E+308) <= ' || max_cost_per_ton_constraint, '')  || '
					' || coalesce(' and coalesce(' || percent_reduction_sql || ' / 100 * ' || annualized_uncontrolled_emis_sql || ' * ' || chained_gdp_adjustment_factor || '
						* ' || get_strategt_cost_inner_sql || '.computed_cost_per_ton, -1E+308) <= ' || max_ann_cost_constraint, '')  || '
			)' else '' end || '

			' || case when include_unspecified_costs = true then '' else '
			and (select sum(' || replace(replace(replace(replace(replace(replace(get_strategt_cost_inner_sql,'inv.','inv2.'),'er.','er2.'),'et.','et2.'),'eq.','eq2.'),'gdplev.','gdplev2.'),'gdplev_incr.','gdplev_incr2.') || '.annual_cost) as annual_cost

			FROM emissions.' || inv_table_name || ' inv2

				inner join emf.pollutants p2
				on p2.name = inv2.poll
				
				left outer join emf.control_measure_equations eq2
				on eq2.control_measure_id = m.id
				and eq2.pollutant_id = p2.id

				left outer join emf.equation_types et2
				on et2.id = eq2.equation_type_id

				left outer join reference.gdplev gdplev2
				on gdplev2.annual = eq2.cost_year

				inner join emf.control_measure_efficiencyrecords er2
				on er2.control_measures_id = m.id
				-- pollutant filter
				and er2.pollutant_id = p2.id
				-- min and max emission filter
				and ' || replace(annualized_uncontrolled_emis_sql,'inv.','inv2.') || ' between coalesce(er2.min_emis, -1E+308) and coalesce(er2.max_emis, 1E+308)
				-- locale filter
				and (er2.locale = inv2.fips or er2.locale = substr(inv2.fips, 1, 2) or er2.locale = '''')
				-- effecive date filter
				and ' || inventory_year || '::integer >= coalesce(date_part(''year'', er2.effective_date), ' || inventory_year || '::integer)		
				-- Replacement vs Add On Logic...
				and (

					-- Measure is Add On Only!
					(
						(coalesce(er2.existing_measure_abbr, '''') <> '''' or er2.existing_dev_code <> 0)
						and 
						(
							(length(inv2.control_measures) > 0 and strpos('','' || coalesce(inv2.control_measures, '''') || '','', '','' || er2.existing_measure_abbr || '','') > 0) '
							|| case when has_cpri_column then ' or (inv2.cpri <> 0 and er2.existing_dev_code = inv2.cpri) '
							when has_primary_device_type_code_column then ' or (length(inv2.primary_device_type_code) > 0 and er2.existing_dev_code || '''' = inv2.primary_device_type_code) '
							else '' end || '
						)
					)

					-- Measure is Replacement Only!
					or 
					(
						coalesce(er2.existing_measure_abbr, '''') = '''' and coalesce(er2.existing_dev_code, 0) = 0
					)

				)

				left outer join reference.gdplev gdplev_incr2
				on gdplev_incr2.annual = er2.cost_year

			where 	' || replace(inv_filter,'inv.','inv2.') || coalesce(replace(county_dataset_filter_sql,'inv.','inv2.'), '') || '

				-- limit to specific source
				and inv2.fips = inv.fips
				and inv2.scc = inv.scc
				' || case when is_point_table = false then '' else '
				and inv2.plantid = inv.plantid
				and inv2.pointid = inv.pointid
				and inv2.stackid = inv.stackid
				and inv2.segment = inv.segment
				' end || '

				-- dont include sources that have no emissions...
				and ' || replace(replace(uncontrolled_emis_sql,'inv.','inv2.'),'invpm25or10.','invpm25or10_2.') || ' <> 0.0

				-- dont include sources that have been fully controlled...
				and coalesce(100 * inv2.ceff / 100 * coalesce(inv2.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv2.rpen / 100, 1.0)' else '' end || ', 0) <> 100.0

			) > 0.0
			' end || '

		order by inv.record_id, 
			er.control_measures_id, case when length(locale) = 5 then 0 when length(locale) = 2 then 1 else 2 end, ' || remaining_emis_sql || ', ' || get_strategt_cost_inner_sql || '.computed_cost_per_ton
	) tbl
		order by scc, fips, ' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || 'poll, emis_reduction, annual_cost
	';

	get diagnostics gimme_count = row_count;
	raise notice '%', 'add target pollutant records ' || gimme_count || ' - ' || clock_timestamp();

	execute 'insert into strat_worksheet (
			cm_abbrev,
			poll,
			scc,
			fips,
			' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '
			annual_oper_maint_cost,
			annual_variable_oper_maint_cost,
			annual_fixed_oper_maint_cost,
			annualized_capital_cost,
			total_capital_cost,
			annual_cost,
			ann_cost_per_ton,
			control_eff,
			rule_pen,
			rule_eff,
			percent_reduction,
			final_emissions,
			Inv_Ctrl_Eff,
			Inv_Rule_Pen,
			Inv_Rule_Eff,
			emis_reduction,
			inv_emissions,
			input_emis,
			output_emis,
			sic,
			naics,
			source_id,
			input_ds_id,
			cm_id,
			equation_type,
			original_dataset_id,
			sector,
			source,
			xloc,
			yloc,
			plant,
			REPLACEMENT_ADDON,
			EXISTING_MEASURE_ABBREVIATION,
			EXISTING_PRIMARY_DEVICE_TYPE_CODE) 
	select DISTINCT ON (inv.record_id, er.control_measures_id) 
		m.abbreviation,
		inv.poll,
		inv.scc,
		inv.fips,
		' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.operation_maintenance_cost as operation_maintenance_cost,
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.variable_operation_maintenance_cost as annual_variable_oper_maint_cost,
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.fixed_operation_maintenance_cost as annual_fixed_oper_maint_cost,
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.annualized_capital_cost as annualized_capital_cost,
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.capital_cost as capital_cost,
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.annual_cost as ann_cost,
		' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.computed_cost_per_ton as computed_cost_per_ton,
		efficiency,
		' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' as rule_pen,
		' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' as rule_eff,
		' || percent_reduction_sql || ' as percent_reduction,
		case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * (1 - ' || percent_reduction_sql || ' / 100) as final_emissions,
		inv.ceff,
		' || case when has_rpen_column then 'coalesce(inv.rpen, 100.0::double precision)' else '100.0::double precision' end || ',
		coalesce(inv.reff, 100.0::double precision),
		' || emis_sql || ' - case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * (1 - ' || percent_reduction_sql || ' / 100) as emis_reduction,
		' || emis_sql || ' as inv_emissions,
		case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end as input_emis,
		case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * (1 - ' || percent_reduction_sql || ' / 100) as output_emis,
		' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ',
		' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ',
		' || case when not has_merged_columns then 'inv.record_id::integer' else 'inv.original_record_id::integer' end || ' as source_id,
		' || intInputDatasetId || '::integer as input_ds_id,
		er.control_measures_id as cm_id,
		' || get_strategt_cost_inner_sql || '.actual_equation_type as equation_type,
		null::integer as original_dataset_id, ' || quote_literal('sector') || '::character varying as sector,
		/*null::integer*/sources.id as source,
		' || case when has_latlong_columns then 'inv.xloc,inv.yloc,' else 'fipscode.centerlon as xloc,fipscode.centerlat as yloc,' end || '
		' || case when has_plant_column then 'inv.plant' else 'null::character varying as plant' end || ',
		measure.REPLACEMENT_ADDON,
		measure.EXISTING_MEASURE_ABBREVIATION,
		measure.EXISTING_PRIMARY_DEVICE_TYPE_CODE

	FROM emissions.' || inv_table_name || ' inv
		' || case when not has_latlong_columns then 'left outer join reference.fips fipscode
		on fipscode.state_county_fips = inv.fips
		and fipscode.country_num = ''0''' else '' end || '

		inner join emf.sources
		on sources.source = inv.scc || inv.fips || ' || case when is_point_table = false then 'repeat('' '', 60) ' else 'rpad(coalesce(inv.plantid, ''''), 15) || rpad(coalesce(inv.pointid, ''''), 15) || rpad(coalesce(inv.stackid, ''''), 15) || rpad(coalesce(inv.segment, ''''), 15)' end || '
/*		on sources.scc = inv.scc
		and sources.fips = inv.fips
		' || case when is_point_table = false then '
		and sources.plantid is null
		and sources.pointid is null
		and sources.stackid is null
		and sources.segment is null
		' else '
		and sources.plantid = inv.plantid
		and sources.pointid = inv.pointid
		and sources.stackid = inv.stackid
		and sources.segment = inv.segment
		' end || '
*/		inner join (
			select source, cm_id, REPLACEMENT_ADDON, EXISTING_MEASURE_ABBREVIATION, EXISTING_PRIMARY_DEVICE_TYPE_CODE
			from emissions.' || worksheet_table_name || '
		) measure
		on measure.source = sources.id

		inner join emf.control_measures m
		on m.id = measure.cm_id --scc.control_measures_id

		' || case when measures_count > 0 then '
		inner join emf.control_strategy_measures csm
		on csm.control_measure_id = m.id
		and csm.control_strategy_id = ' || intControlStrategyId || '
		' else '' end || '

		inner join emf.pollutants p
		on p.name = inv.poll

		inner join emf.control_measure_efficiencyrecords er
		on er.control_measures_id = m.id
		-- pollutant filter
		and er.pollutant_id = p.id
		-- min and max emission filter
		and ' || annualized_uncontrolled_emis_sql || ' between coalesce(er.min_emis, -1E+308::double precision) and coalesce(er.max_emis, 1E+308::double precision)
		-- locale filter
		and (er.locale = inv.fips or er.locale = substr(inv.fips, 1, 2) or er.locale = ''''::varchar(6))
		-- effecive date filter
		and ' || inventory_year || '::integer >= coalesce(date_part(''year'', er.effective_date)::integer, ' || inventory_year || '::integer)
		-- Replacement vs Add On Logic...
		and (

			-- Measure is Add On Only!
			(
				measure.REPLACEMENT_ADDON = ''A''
				and 
				(measure.EXISTING_MEASURE_ABBREVIATION = er.existing_measure_abbr or measure.EXISTING_PRIMARY_DEVICE_TYPE_CODE = er.existing_dev_code || '''')
			)

			-- Measure is Replacement Only!
			or 
			(
				measure.REPLACEMENT_ADDON = ''R''
			)

		)

		left outer join reference.gdplev gdplev_incr
		on gdplev_incr.annual = er.cost_year

		inner join emf.control_measure_months ms
		on ms.control_measure_id = m.id
		and ms.month in (0::integer' || case when dataset_month != 0 then ',' || dataset_month || '::integer' else '' end || ')

		left outer join emf.control_measure_equations eq
		on eq.control_measure_id = m.id
		and eq.pollutant_id = p.id

		left outer join emf.equation_types et
		on et.id = eq.equation_type_id

		left outer join reference.gdplev
		on gdplev.annual = eq.cost_year

	where 	p.id <> ' || target_pollutant_id || '
		and ' || uncontrolled_emis_sql || ' <> 0.0
--		and coalesce(100 * inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0) <> 100.0

	order by inv.record_id, 
		er.control_measures_id, case when length(locale) = 5 then 0 when length(locale) = 2 then 1 else 2 end, ' || percent_reduction_sql || ' desc, ' || get_strategt_cost_inner_sql || '.computed_cost_per_ton
	';
	
	get diagnostics gimme_count = row_count;
	raise notice '%', 'add cobenefit pollutant records ' || gimme_count || ' - ' || clock_timestamp();
	raise notice '%', 'inv ' || inv_table_name || ' ws ' || worksheet_table_name || ' - ' || clock_timestamp();

--	SET enable_seqscan TO 'on';
--	SET enable_nestloop TO 'on';

	execute 'select count(1) from emissions.' || worksheet_table_name || ''
	into gimme_count;
	raise notice '%', 'All Controls - emissions.' || worksheet_table_name || ' count = ' || gimme_count || ' - ' || clock_timestamp();

return;




END;
$$ LANGUAGE plpgsql;