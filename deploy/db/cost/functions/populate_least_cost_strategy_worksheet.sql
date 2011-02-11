drop FUNCTION public.populate_least_cost_strategy_worksheet(integer, 
	integer, 
	integer);

CREATE OR REPLACE FUNCTION public.populate_least_cost_strategy_worksheet(int_control_strategy_id integer, 
	input_dataset_id integer, 
	input_dataset_version integer) RETURNS void AS $$
DECLARE
	strategy_name varchar(255) := '';
	inv_table_name varchar(64) := '';
	inv_filter text := '';
	inv_fips_filter text := '';
	worksheet_dataset_id integer := null;
	worksheet_table_name varchar(64) := '';
	county_dataset_id integer := null;
	county_dataset_version integer := null;
	region RECORD;
	target_pollutant_id integer := 0;
	target_pollutant varchar;
	measures_count integer := 0;
	measure_with_region_count integer := 0;
	measure_classes_count integer := 0;
	county_dataset_filter_sql text := '';
	cost_year integer := null;
	inventory_year integer := null;
	is_point_table boolean := false;
	use_cost_equations boolean := false;
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
	has_rpen_column boolean := false;
	has_merged_columns boolean := false;
	has_latlong_columns boolean := false;
	has_plant_column boolean := false;
	gimme_count integer := 0;
	min_emis_reduction_constraint real := null;
	min_control_efficiency_constraint real := null;
	max_cost_per_ton_constraint real := null;
	max_ann_cost_constraint real := null;
	replacement_control_min_eff_diff_constraint double precision := null;
	has_constraints boolean := null;
	str varchar;
	marginal double precision;
	emis_reduction double precision;
	record_id double precision;
	apply_order integer;
	domain_wide_emis_reduction double precision;
	domain_wide_pct_reduction double precision;
	domain_wide_pct_reduction_increment double precision;
	domain_wide_pct_reduction_start double precision;
	domain_wide_pct_reduction_end double precision;
	counter integer := 0;
	record_count integer := 0;
	deleted_record_count integer := 0;
	increasing_trend boolean := false;
	prev_apply_order integer;
	get_strategt_cost_sql character varying;
	get_strategt_cost_inner_sql character varying;
	annualized_uncontrolled_emis_sql character varying;
	uncontrolled_emis_sql character varying;
	emis_sql character varying;
	percent_reduction_sql character varying;
	sector character varying := '';
	strategy_type character varying(255);
	include_unspecified_costs boolean := true; 
	has_cpri_column boolean := false; 
	has_primary_device_type_code_column boolean := false; 
	remaining_emis_sql character varying;
	creator_user_id integer := 0;
	is_cost_su boolean := false; 
BEGIN
--	SET work_mem TO '256MB';
--	SET enable_seqscan TO 'off';
--	SET enable_nestloop TO 'on';

	raise notice '%', 'start public.populate_least_cost_strategy_worksheet ' || clock_timestamp();

	-- get the input dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = input_dataset_id
	into inv_table_name;

	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
		inner join emf.strategy_result_types srt
		on srt.id = sr.strategy_result_type_id
	where sr.control_strategy_id = int_control_strategy_id 
		and srt.name = 'Least Cost Control Measure Worksheet'
	into worksheet_dataset_id,
		worksheet_table_name;

	-- see if control strategy has only certain measures specified
	SELECT count(id), 
		count(case when region_dataset_id is not null then 1 else null end)
	FROM emf.control_strategy_measures 
	where control_strategy_measures.control_strategy_id = int_control_strategy_id 
	INTO measures_count, 
		measure_with_region_count;

	-- see if measure classes were specified
	IF measures_count = 0 THEN
		SELECT count(1)
		FROM emf.control_strategy_classes 
		where control_strategy_classes.control_strategy_id = int_control_strategy_id
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
		st."name",
		coalesce(cs.include_unspecified_costs,true),
		cs.creator_id
	FROM emf.control_strategies cs
		inner join emf.strategy_types st
		on st.id = cs.strategy_type_id
	where cs.id = int_control_strategy_id
	INTO strategy_name,
		target_pollutant_id,
		inv_filter,
		cost_year,
		inventory_year,
		county_dataset_id,
		county_dataset_version,
		use_cost_equations,
		discount_rate,
		strategy_type,
		include_unspecified_costs,
		creator_user_id;

	-- get target pollutant name
	select name
	from emf.pollutants
	where id = target_pollutant_id
	into target_pollutant;


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

	-- see if this a merged orl inventory
	has_merged_columns := public.check_table_for_columns(inv_table_name, 'original_dataset_id,sector', ',');

	-- see if there is plant column in the inventory
	has_cpri_column := public.check_table_for_columns(inv_table_name, 'cpri', ',');

	-- see if there is primary_device_type_code column in the inventory
	has_primary_device_type_code_column := public.check_table_for_columns(inv_table_name, 'primary_device_type_code', ',');

	-- get sector of the inventory if this is not a merged orl inventory
	--get the inventory sector(s)
	select public.concatenate_with_ampersand(distinct name)
	from emf.sectors s
		inner join emf.datasets_sectors ds
		on ds.sector_id = s.id
	where ds.dataset_id = input_dataset_id
	into sector;

	-- get strategy constraints
	SELECT csc.max_emis_reduction,
		csc.max_control_efficiency,
		csc.min_cost_per_ton,
		csc.min_ann_cost,
		csc.domain_wide_emis_reduction,
		csc.domain_wide_pct_reduction,
		csc.replacement_control_min_eff_diff/*,
		csc.domain_wide_pct_reduction_increment,
		csc.domain_wide_pct_reduction_start,
		csc.domain_wide_pct_reduction_end*/
	FROM emf.control_strategy_constraints csc
	where csc.control_strategy_id = int_control_strategy_id
	INTO min_emis_reduction_constraint,
		min_control_efficiency_constraint,
		max_cost_per_ton_constraint,
		max_ann_cost_constraint,
		domain_wide_emis_reduction,
		domain_wide_pct_reduction,
		replacement_control_min_eff_diff_constraint/*,
		domain_wide_pct_reduction_increment,
		domain_wide_pct_reduction_start,
		domain_wide_pct_reduction_end*/;

/*	IF coalesce(domain_wide_pct_reduction_increment, 0.0) = 0.0 THEN
		RAISE EXCEPTION 'Missing domain wide percentage reduction increment.';
		return;
	END IF;
*/
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

	-- if strategy has specific measures assigned, then store these in a temp table for later use...
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
			where m.control_strategy_id = ' || int_control_strategy_id || '
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

	--do this only for the Least Cost strategy type...
	IF strategy_type = 'Least Cost' THEN
		-- get strategy constraints
		SELECT csc.domain_wide_emis_reduction,
			csc.domain_wide_pct_reduction
		FROM emf.control_strategy_constraints csc
		where csc.control_strategy_id = int_control_strategy_id
		INTO domain_wide_emis_reduction,
		domain_wide_pct_reduction;

		IF coalesce(domain_wide_emis_reduction, domain_wide_pct_reduction, 0.0) = 0.0 THEN
			RAISE EXCEPTION 'Missing domain wide emission (or percentage) reduction.';
			return;
		END IF;
		-- figure out domain_wide_emis_reduction, if pct red was passed in
		IF coalesce(domain_wide_pct_reduction, 0.0) <> 0.0 THEN
			execute 'select ' || domain_wide_pct_reduction || ' / 100.0 * sum(' || case when dataset_month != 0 then 'coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis)' else 'inv.ann_emis' end || ') 
			FROM emissions.' || inv_table_name || ' as inv
			where ' || inv_filter || county_dataset_filter_sql || '
				and poll = ' || quote_literal(target_pollutant)
			into domain_wide_emis_reduction;

			-- update so its viewable for client, via the constraints tab
			execute 'update emf.control_strategy_constraints 
			set domain_wide_emis_reduction = ' || coalesce(domain_wide_emis_reduction || '', 'null::double precision') || '
			where control_strategy_id = ' || int_control_strategy_id;
		ELSE
			execute 'select ' || domain_wide_emis_reduction || ' / sum(' || case when dataset_month != 0 then 'coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis)' else 'inv.ann_emis' end || ') * 100.0 
			FROM emissions.' || inv_table_name || ' as inv
			where ' || inv_filter || county_dataset_filter_sql || '
				and poll = ' || quote_literal(target_pollutant)
			into domain_wide_pct_reduction;

			-- update so its viewable for client, via the constraints tab
			execute 'update emf.control_strategy_constraints 
			set domain_wide_pct_reduction = ' || coalesce(domain_wide_pct_reduction || '', 'null::double precision') || '
			where control_strategy_id = ' || int_control_strategy_id;
		END IF;
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

	--TO DO - tempoprary shouldn't be used in production
--	execute 'truncate emissions.' || worksheet_table_name;

--	uncontrolled_emis_sql := case when dataset_month != 0 then 'coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis)' else 'inv.ann_emis' end;
--	annualized_emis_sql := case when dataset_month != 0 then 'coalesce(inv.avd_emis * 365, inv.ann_emis)' else 'inv.ann_emis' end;


	uncontrolled_emis_sql := '(' ||
			case 
				when dataset_month != 0 then 
					'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
				else 
					'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then inv.ann_emis / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
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
					'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.avd_emis * 365, inv.ann_emis) / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
				else 
					'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then inv.ann_emis / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
			end;
	percent_reduction_sql := 'er.efficiency * ' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' * ' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' / 100 / 100';
	-- relative emission reduction from inventory emission (i.e., ann emis in inv 100 tons (add on control gives addtl 50% red --> 50 tons
	-- whereas a 95% ceff replacement control on a source with an existing control of 90% reduction needs to back out source to an 
	-- uncontrolled emission 100 / (1 - 0.9) = 1000 tons giving a and then applying new control gives 950 tons reduced giving a 95% control
	remaining_emis_sql := 
		'( case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then -- add on control
			' || emis_sql || ' * ' || percent_reduction_sql || ' / 100.0 
		else -- replacement control
			(' || uncontrolled_emis_sql || ' * (1.0 - ' || percent_reduction_sql || ' / 100.0))
		end )';
	get_strategt_cost_sql := '(public.get_strategy_costs(' || case when use_cost_equations then 'true' else 'false' end || '::boolean, m.id, 
			abbreviation, ' || discount_rate|| ', 
			m.equipment_life, er.cap_ann_ratio, 
			er.cap_rec_factor, er.ref_yr_cost_per_ton, 
			case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * ' || percent_reduction_sql || ' / 100, ' || ref_cost_year_chained_gdp || ' / cast(gdplev.chained_gdp as double precision), 
			' || case when use_cost_equations then 
			'et.name, 
			eq.value1, eq.value2, 
			eq.value3, eq.value4, 
			eq.value5, eq.value6, 
			eq.value7, eq.value8, 
			eq.value9, eq.value10, 
			' || case when not is_point_table then 'null' else 'inv.stkflow * 60.0' end || ', ' || case when not is_point_table then 'null' else case when not has_design_capacity_columns then 'null' else 'inv.design_capacity' end end || ', 
			' || case when not is_point_table then 'null' else case when not has_design_capacity_columns then 'null' else 'inv.design_capacity_unit_numerator' end end || ', ' || case when not is_point_table then 'null' else case when not has_design_capacity_columns then 'null' else 'inv.design_capacity_unit_denominator' end end || ', ' || case when not is_point_table then 'null' else 'inv.annual_avg_hours_per_year' end 
			else
			'null, 
			null, null, 
			null, null, 
			null, null, 
			null, null, 
			null, null, 
			null, null, 
			null, null, 
			null'
			end
			|| ',inv.ceff, ' || ref_cost_year_chained_gdp || '::double precision / gdplev_incr.chained_gdp::double precision * er.incremental_cost_per_ton))';
	get_strategt_cost_inner_sql := replace(get_strategt_cost_sql,'m.control_measures_id','m.id');


/*	EXECUTE '
		CREATE TEMP TABLE emf.sources (id serial NOT NULL, source varchar(76) NOT NULL) ON COMMIT DROP;
		CREATE INDEX sourceIds_source ON sourceIds USING btree (source);
		CREATE INDEX sourceIds_id ON sourceIds USING btree (id);
		ALTER TABLE sourceIds CLUSTER ON sourceIds_id;
	';
	execute 'insert into emf.sources (scc, fips ' || case when is_point_table = false then '' else ', plantid, pointid, stackid, segment' end || ', source)
		select distinct on (inv.scc, inv.fips ' || case when is_point_table = false then '' else ', inv.plantid, inv.pointid, inv.stackid, inv.segment' end || ')
		inv.scc, inv.fips ' || case when is_point_table = false then '' else ', inv.plantid, inv.pointid, inv.stackid, inv.segment' end || ',
		inv.scc || inv.fips || ' || case when is_point_table = false then 'rpad('''', 60)' else 'rpad(coalesce(inv.plantid, ''''), 15) || rpad(coalesce(inv.pointid, ''''), 15) || rpad(coalesce(inv.stackid, ''''), 15) || rpad(coalesce(inv.segment, ''''), 15)' end || '
	FROM emissions.' || inv_table_name || ' inv
		left outer join emf.sources
		on sources.scc = inv.scc
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
	where 	' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
		and inv.poll = ' || quote_literal(target_pollutant) || '
		and sources.id is null';
	raise notice '%', 'populate emf.sources table ' || clock_timestamp();
*/

	-- add target pollutant records
	execute 'insert into emissions.' || worksheet_table_name || ' (
			Dataset_Id,
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
			marginal,
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
			' || worksheet_dataset_id || ' as Dataset_Id,
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
			' || case when not has_merged_columns then 'inv.record_id::integer' else 'inv.original_record_id::integer' end || ' as source_id,
			' || input_dataset_id || '::integer as input_ds_id,
			er.control_measures_id as cm_id,
			case when coalesce(case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * ' || percent_reduction_sql || ' / 100, 0) != 0 then coalesce(' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_inner_sql || '.annual_cost / (case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * ' || percent_reduction_sql || ' / 100), 0.0) else 0.0 end as marginal,
			' || get_strategt_cost_inner_sql || '.actual_equation_type as equation_type,
			' || case when not has_merged_columns then 'null::integer as original_dataset_id, ' || quote_literal(sector) || '::text as sector' else 'inv.original_dataset_id, inv.sector' end || '
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
			and csm.control_strategy_id = ' || int_control_strategy_id || '::integer
			' else '' end || '

			' || case when measures_count = 0 and measure_classes_count > 0 then '
			inner join emf.control_strategy_classes csc
			on csc.control_measure_class_id = m.cm_class_id
			and csc.control_strategy_id = ' || int_control_strategy_id || '::integer
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

	execute 'insert into emissions.' || worksheet_table_name || ' (
			Dataset_Id,
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
			marginal,
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
		' || worksheet_dataset_id || ',
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
		' || input_dataset_id || '::integer as input_ds_id,
		er.control_measures_id as cm_id,
		case when coalesce(case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * ' || percent_reduction_sql || ' / 100, 0) != 0 then coalesce(' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_inner_sql || '.annual_cost / case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * ' || percent_reduction_sql || ' / 100, 0.0) else 0.0 end as marginal,
		' || get_strategt_cost_inner_sql || '.actual_equation_type as equation_type,
		' || case when not has_merged_columns then 'null::integer as original_dataset_id, ' || quote_literal(sector) || '::character varying as sector' else 'inv.original_dataset_id, inv.sector' end || ',
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
		and csm.control_strategy_id = ' || int_control_strategy_id || '
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

	execute 'update emissions.' || worksheet_table_name || ' as detailed_result
		set source_annual_cost = dr.annual_cost,
			marginal = case when coalesce(emis_reduction, 0) != 0 then coalesce(dr.annual_cost / emis_reduction, 0.0) else 0.0 end
		from (
			select dr.source, dr.cm_id, sum(annual_cost) as annual_cost
			from emissions.' || worksheet_table_name || ' dr
			group by dr.source, dr.cm_id
			having count(1) > 1) dr
		where 
			dr.source = detailed_result.source
			and dr.cm_id = detailed_result.cm_id
			and detailed_result.poll = ' || quote_literal(target_pollutant) || '';
	raise notice '%', 'make sure the annual cost includes cost for the cobenefit pollutants - ' || clock_timestamp();

	return;
END;
$$ LANGUAGE plpgsql;

