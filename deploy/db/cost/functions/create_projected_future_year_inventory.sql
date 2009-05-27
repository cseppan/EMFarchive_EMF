
--select public.create_projected_future_year_inventory(81, 399, 0, 2750, 2822)

CREATE OR REPLACE FUNCTION public.create_projected_future_year_inventory(
	control_strategy_id integer, 
	input_dataset_id integer, 
	input_dataset_version integer, 
	strategy_result_id integer, 
	output_dataset_id integer
	) RETURNS void AS
$BODY$
DECLARE
	inv_table_name varchar(64) := '';
	cont_inv_table_name varchar(64) := '';
	inv_filter varchar := '';
	inv_fips_filter text := '';
	detailed_result_dataset_id integer := null;
	detailed_result_table_name varchar(64) := '';
	county_dataset_id integer := null;
	county_dataset_version integer := null;
	region RECORD;
	control_program RECORD;
	target_pollutant_id integer := 0;
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
	has_rpen_column boolean := false;
	has_cpri_column boolean := false;
	has_primary_device_type_code_column boolean := false;
	annualized_emis_sql character varying;
	annual_emis_sql character varying;
	percent_reduction_sql character varying;
	insert_column_list_sql character varying := '';
	select_column_list_sql character varying := '';
	column_name character varying;
	get_strategt_cost_sql character varying;
	get_strategt_cost_inner_sql character varying;
	has_control_measures_col boolean := false;
	has_pct_reduction_col boolean := false;
	has_current_cost_col boolean := false;
	has_cumulative_cost_col boolean := false;
BEGIN

	-- get the input dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = input_dataset_id
	into inv_table_name;

	-- get the cont inv dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = output_dataset_id
	into cont_inv_table_name;

	-- get the detailed result dataset info
	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
	where sr.id = strategy_result_id
	into detailed_result_dataset_id,
		detailed_result_table_name;

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
		cs.discount_rate / 100
	FROM emf.control_strategies cs
	where cs.id = control_strategy_id
	INTO target_pollutant_id,
		inv_filter,
		cost_year,
		inventory_year,
		county_dataset_id,
		county_dataset_version,
		use_cost_equations,
		discount_rate;

	-- see if there are point specific columns in the inventory
	is_point_table := public.check_table_for_columns(inv_table_name, 'plantid,pointid,stackid,segment', ',');

	-- see if there is a sic column in the inventory
	has_sic_column := public.check_table_for_columns(inv_table_name, 'sic', ',');

	-- see if there is a naics column in the inventory
	has_naics_column := public.check_table_for_columns(inv_table_name, 'naics', ',');

	-- see if there is a rpen column in the inventory
	has_rpen_column := public.check_table_for_columns(inv_table_name, 'rpen', ',');

	-- see if there is a cpri column in the inventory
	has_cpri_column := public.check_table_for_columns(inv_table_name, 'cpri', ',');

	-- see if there is a primary_device_type_code column in the inventory
	has_primary_device_type_code_column := public.check_table_for_columns(inv_table_name, 'primary_device_type_code', ',');

	has_design_capacity_columns := public.check_table_for_columns(inv_table_name, 'design_capacity,design_capacity_unit_numerator,design_capacity_unit_denominator', ',');

	-- see if there is a pct_reduction column in the inventory
	has_pct_reduction_col := public.check_table_for_columns(inv_table_name, 'pct_reduction', ',');

	-- see if there is a control_measures column in the inventory
	has_control_measures_col := public.check_table_for_columns(inv_table_name, 'control_measures', ',');

	-- see if there is a current_cost column in the inventory
	has_current_cost_col := public.check_table_for_columns(inv_table_name, 'current_cost', ',');

	-- see if there is a cumulative_cost column in the inventory
	has_cumulative_cost_col := public.check_table_for_columns(inv_table_name, 'cumulative_cost', ',');

	-- get strategy constraints
	SELECT max_emis_reduction,
		max_control_efficiency,
		min_cost_per_ton,
		min_ann_cost
	FROM emf.control_strategy_constraints csc
	where csc.control_strategy_id = control_strategy_id
	INTO min_emis_reduction_constraint,
		min_control_efficiency_constraint,
		max_cost_per_ton_constraint,
		max_ann_cost_constraint;

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
	inv_filter := '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version) || ')' || coalesce(' and ' || inv_filter, '');

	raise notice '%', 'start ' || clock_timestamp();

--	annual_emis_sql := case when dataset_month != 0 then 'coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis)' else 'inv.ann_emis' end;
--	annualized_emis_sql := case when dataset_month != 0 then 'coalesce(inv.avd_emis * 365, inv.ann_emis)' else 'inv.ann_emis' end;
	annual_emis_sql := 
			case 
				when dataset_month != 0 then 
					'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
				else 
					'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then inv.ann_emis / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
			end;
	annualized_emis_sql := 
			case 
				when dataset_month != 0 then 
					'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.avd_emis * 365, inv.ann_emis) / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
				else 
					'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then inv.ann_emis / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
			end;
	percent_reduction_sql := 'er.efficiency * ' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' * ' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' / 100 / 100';
	get_strategt_cost_sql := '(public.get_strategy_costs(' || case when use_cost_equations then 'true' else 'false' end || '::boolean, m.control_measures_id, 
			abbreviation, ' || discount_rate|| ', 
			m.equipment_life, er.cap_ann_ratio, 
			er.cap_rec_factor, er.ref_yr_cost_per_ton, 
			' || annual_emis_sql || ' * ' || percent_reduction_sql || ' / 100, ' || ref_cost_year_chained_gdp || ' / cast(chained_gdp as double precision), 
			' || case when use_cost_equations then 
			'et.name, 
			eq.value1, eq.value2, 
			eq.value3, eq.value4, 
			eq.value5, eq.value6, 
			eq.value7, eq.value8, 
			eq.value9, eq.value10, 
			' || case when not is_point_table then 'null' else 'inv.stkflow' end || ', ' || case when not is_point_table then 'null' else case when not has_design_capacity_columns then 'null' else 'inv.design_capacity' end end || ', 
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

	-- build insert column list and select column list
	FOR region IN EXECUTE 
		'SELECT a.attname
	FROM pg_class c
		inner join pg_attribute a
		on a.attrelid = c.oid
		inner join pg_type t
		on t.oid = a.atttypid
	WHERE c.relname = ' || quote_literal(cont_inv_table_name) || '
		AND a.attnum > 0'
	LOOP
		column_name := region.attname;
		
		IF column_name = 'record_id' THEN
--			select_column_list_sql := select_column_list_sql || 'inv.record_id';
--			insert_column_list_sql := insert_column_list_sql || column_name;
		ELSIF column_name = 'dataset_id' THEN
			select_column_list_sql := select_column_list_sql || '' || output_dataset_id || ' as dataset_id';
			insert_column_list_sql := insert_column_list_sql || '' || column_name;
		ELSIF column_name = 'delete_versions' THEN
			select_column_list_sql := select_column_list_sql || ','''' as delete_versions';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'version' THEN
			select_column_list_sql := select_column_list_sql || ',0 as version';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'design_capacity' THEN
			select_column_list_sql := select_column_list_sql || ',' || case when has_design_capacity_columns then 'design_capacity' else 'null::double precision as design_capacity' end;
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'design_capacity_unit_numerator' THEN
			select_column_list_sql := select_column_list_sql || ',' || case when has_design_capacity_columns then 'design_capacity_unit_numerator' else 'null::character varying(10) as design_capacity_unit_numerator' end;
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'design_capacity_unit_denominator' THEN
			select_column_list_sql := select_column_list_sql || ',' || case when has_design_capacity_columns then 'design_capacity_unit_denominator' else 'null::character varying(10) as design_capacity_unit_denominator' end;
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'current_cost' THEN
			select_column_list_sql := select_column_list_sql || ',' || case when has_current_cost_col then 'current_cost' else 'null::double precision as current_cost' end;
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'cumulative_cost' THEN
			select_column_list_sql := select_column_list_sql || ',' || case when has_cumulative_cost_col then 'cumulative_cost' else 'null::double precision as cumulative_cost' end;
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'control_measures' THEN
			select_column_list_sql := select_column_list_sql || ',' || case when has_control_measures_col then '
			case 
				when control_measures is null or length(control_measures) = 0 then 
					case 
						when cr.cm_abbrev is not null then cr.cm_abbrev
						when proj.cm_abbrev is not null and cont.cm_abbrev is not null then proj.cm_abbrev || ''&'' || cont.cm_abbrev 
						when proj.cm_abbrev is null and cont.cm_abbrev is not null then cont.cm_abbrev 
						when proj.cm_abbrev is not null and cont.cm_abbrev is null then proj.cm_abbrev 
						else ''''::character varying 
					end 
				else control_measures || coalesce(''&'' || cr.cm_abbrev, '''')  || coalesce(''&'' || proj.cm_abbrev, '''') || coalesce(''&'' || cont.cm_abbrev, '''') 
			end' else '
			case 
				when cr.cm_abbrev is not null then cr.cm_abbrev
				when proj.cm_abbrev is not null and cont.cm_abbrev is not null then proj.cm_abbrev || ''&'' || cont.cm_abbrev 
				when proj.cm_abbrev is null and cont.cm_abbrev is not null then cont.cm_abbrev 
				when proj.cm_abbrev is not null and cont.cm_abbrev is null then proj.cm_abbrev 
				else ''''::character varying 
			end ' end || ' as control_measures';
--			select_column_list_sql := select_column_list_sql || ', case when control_measures is null or length(control_measures) = 0 then abbreviation else control_measures || ''&'' || abbreviation end as control_measures';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'pct_reduction' THEN
			select_column_list_sql := select_column_list_sql || ',' || case when has_pct_reduction_col then '
			case 
				when pct_reduction is null or length(pct_reduction) = 0 then 
					case 
						when cr.adj_factor is not null then cr.adj_factor::character varying
						when proj.adj_factor is not null and cont.percent_reduction is not null then proj.adj_factor || ''&'' || cont.percent_reduction 
						when proj.adj_factor is null and cont.percent_reduction is not null then cont.percent_reduction::character varying
						when proj.adj_factor is not null and cont.percent_reduction is null then proj.adj_factor::character varying
						else ''''::character varying 
					end 
				else pct_reduction || coalesce(''&'' || proj.adj_factor, '''') || coalesce(''&'' || cont.percent_reduction, '''') 
			end' else '
			case 
				when cr.adj_factor is not null then cr.adj_factor::character varying
				when proj.adj_factor is not null and cont.percent_reduction is not null then proj.adj_factor || ''&'' || cont.percent_reduction 
				when proj.adj_factor is null and cont.percent_reduction is not null then cont.percent_reduction::character varying
				when proj.adj_factor is not null and cont.percent_reduction is null then proj.adj_factor::character varying
				else ''''::character varying 
			end ' end || ' as pct_reduction';
--			select_column_list_sql := select_column_list_sql || ', case when pct_reduction is null or length(pct_reduction) = 0 then efficiency::text else pct_reduction || ''&'' || efficiency::text end as pct_reduction';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'avd_emis' THEN
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is not null then cr.final_emissions / ' || case when dataset_month <> 0 then no_days_in_month else '365' end || ' 
				when cont.source_id is not null then cont.final_emissions / ' || case when dataset_month <> 0 then no_days_in_month else '365' end || ' 
				when proj.source_id is not null then proj.final_emissions / ' || case when dataset_month <> 0 then no_days_in_month else '365' end || ' 
				else avd_emis 
			end as avd_emis';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'ann_emis' THEN
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is not null then cr.final_emissions 
				when cont.source_id is not null then cont.final_emissions 
				when proj.source_id is not null then proj.final_emissions 
				else ann_emis 
			end as ann_emis';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'ceff' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.percent_reduction <> 0.0 then cont.percent_reduction
				else ceff 
			end as ceff';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'reff' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.percent_reduction <> 0.0 then 100.0::double precision 
				else reff 
			end as reff';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSIF column_name = 'rpen' THEN
			--and cont.ceff <> 0.0 indicates a pass through situation, don't control source
			select_column_list_sql := select_column_list_sql || ', 
			case 
				when cr.source_id is null and cont.source_id is not null and cont.percent_reduction <> 0.0 then 100.0::double precision 
				else rpen 
			end as rpen';
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		ELSE
			select_column_list_sql := select_column_list_sql || ',' || column_name;
			insert_column_list_sql := insert_column_list_sql || ',' || column_name;
		END IF;
--            } else if (columnName.equalsIgnoreCase("avd_emis")) {
--                sql += ", case when b.source_id is not null then b.final_emissions / " + (month != -1 ? noOfDaysInMonth : "365") + " else avd_emis end as avd_emis";
--                columnList += "," + columnName;
--            } else if (columnName.equalsIgnoreCase("ann_emis")) {
--                sql += ", case when b.source_id is not null then b.final_emissions else ann_emis end as ann_emis";
--                columnList += "," + columnName;
	END LOOP;

	select_column_list_sql := replace(
	replace(
	replace(
	replace(
	replace(
	replace(
	replace(
	replace(
	replace(
	replace(
		select_column_list_sql, 
	'scc', 'inv.scc'), 
	'fips', 'inv.fips'), 
	'comments', 'inv.comments'),  
	'pointid', 'inv.pointid'), 
	'stackid', 'inv.stackid'), 
	'segment', 'inv.segment'), 
	'poll', 'inv.poll'), 
	'naics', 'inv.naics'), 
	'sic', 'inv.sic'), 
	'plant', 'inv.plant');

	-- populate the new inventory...work off of new data





	execute 
	--raise notice '%', 
	'insert into emissions.' || cont_inv_table_name|| ' 
		(
		' || insert_column_list_sql || ' 
		)
	select 
		' || select_column_list_sql || ' 
	from emissions.' || inv_table_name || ' inv

		-- deal with projections, an apply order of 1 delineates a projection...
		left outer join (

			SELECT source_id, 
				final_emissions, 
				annual_cost, 
				cm_abbrev, 
				adj_factor
			FROM emissions.' || detailed_result_table_name || '
			where apply_order = 1

		) proj
		on inv.record_id = proj.source_id

		-- deal with controls, an apply order of 2 delineates a control...
		left outer join (

			SELECT source_id, 
				final_emissions, 
				annual_cost, 
				cm_abbrev, 
				percent_reduction
			FROM emissions.' || detailed_result_table_name || '
			where apply_order = 2

		) cont
		on inv.record_id = cont.source_id

		-- deal with caps and replacements, an apply order of 4 and 5 delineates a control, only get the cap if there are both a cap and replacement...
		-- the distinct on source id and order by source_id, apply_order makes sure the cap is the first one to use 
		left outer join (

			SELECT distinct on (source_id) source_id, 
				final_emissions, 
				annual_cost, 
				cm_abbrev, 
				adj_factor
			FROM emissions.' || detailed_result_table_name || '
			where apply_order in (4,5)
			order by source_id, apply_order

		) cr
		on inv.record_id = cr.source_id

	where ' || replace(replace(replace(inv_filter, '(version', '(inv.version'), 'dataset_id', 'inv.dataset_id'), 'delete_versions', 'inv.delete_versions') || coalesce(county_dataset_filter_sql, '') || '

		-- get rid of plant closures records
		and not exists (
			select 1 
			from emissions.' || detailed_result_table_name || ' dr
			where inv.record_id = dr.source_id
				and cm_abbrev = ''PLTCLOSURE'')';

END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;