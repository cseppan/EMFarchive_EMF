

CREATE OR REPLACE FUNCTION public.populate_max_emis_red_strategy_messages(
	control_strategy_id integer, 
	input_dataset_id integer, 
	input_dataset_version integer, 
	message_strategy_result_id integer, 
	detailed_strategy_result_id integer
	) RETURNS void AS
$BODY$
DECLARE
	inv_table_name varchar(64) := '';
	inv_filter varchar := '';
	inv_fips_filter text := '';
	strategy_messages_dataset_id integer := null;
	strategy_messages_table_name varchar(64) := '';

	strategy_detailed_result_dataset_id integer := null;
	strategy_detailed_result_table_name varchar(64) := '';

	county_dataset_id integer := null;
	county_dataset_version integer := null;
	region RECORD;
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
	has_mact_column boolean := false;
	has_target_pollutant integer := 0;
	has_rpen_column boolean := false;
	has_cpri_column boolean := false;
	has_primary_device_type_code_column boolean := false;
	column_name character varying;
	has_control_measures_col boolean := false;
	has_pct_reduction_col boolean := false;
	sql character varying := '';
BEGIN

	-- get the input dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = input_dataset_id
	into inv_table_name;

	-- get the strategy messages result dataset info
	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
	where sr.id = message_strategy_result_id
	into strategy_messages_dataset_id,
		strategy_messages_table_name;

	-- get the detailed result dataset info
	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
	where sr.id = detailed_strategy_result_id
	into strategy_detailed_result_dataset_id,
		strategy_detailed_result_table_name;

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
		case when length(trim(cs.filter)) > 0 then '(' || public.alias_inventory_filter(cs.filter, 'a') || ')' else null end,
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
	
	-- see if there is a mact column in the inventory
	has_mact_column := public.check_table_for_columns(inv_table_name, 'mact', ',');

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

	-- see if there is design capacity columns in the inventory
	has_design_capacity_columns := public.check_table_for_columns(inv_table_name, 'design_capacity,design_capacity_unit_numerator,design_capacity_unit_denominator', ',');

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

	-- see if their was a county dataset specified for the strategy, is so then build a sql where clause filter for later use
	IF county_dataset_id is not null THEN
		county_dataset_filter_sql := ' and a.fips in (SELECT fips
			FROM emissions.' || (SELECT table_name FROM emf.internal_sources where dataset_id = county_dataset_id) || '
			where ' || public.build_version_where_filter(county_dataset_id, county_dataset_version) || ')';
	END IF;
	-- build version info into where clause filter
	inv_filter := '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'a'::character varying(64)) || ')' || coalesce(' and ' || inv_filter, '');


	-- look for negative emissions in detailed result...
	execute
	--raise notice '%',
	 'insert into emissions.' || strategy_messages_table_name || ' 
		(
		dataset_id, 
		fips, 
		scc, 
		plantid, 
		pointid, 
		stackid, 
		segment, 
		poll, 
		status,
		control_program,
		message
		)
	select 
		' || strategy_messages_dataset_id || '::integer,
		dr.fips,
		dr.scc,
		dr.plantid, 
		dr.pointid, 
		dr.stackid, 
		dr.segment, 
		dr.poll,
		''Warning''::character varying(11) as status,
		null::character varying(255) as control_program,
		''Emission reduction is negative, '' || emis_reduction || ''.'' as "comment"
	from emissions.' || strategy_detailed_result_table_name || ' dr
	where 	emis_reduction < 0.0';

	-- if PM target pollutant run, then see if any of the CEFF are missing from the PM 10 vs 2.5 pollutant sources
	execute 'insert into emissions.' || strategy_messages_table_name || ' 
		(
		dataset_id, 
		fips, 
		scc, 
		' || case when is_point_table then '
		plantid, 
		pointid, 
		stackid, 
		segment, 
		' else '' end || ' 
		poll, 
		status,
		control_program,
		message
		)
	select 
		' || strategy_messages_dataset_id || '::integer,
		a.fips,
		a.scc,
		' || case when is_point_table then '
		a.plantid, 
		a.pointid, 
		a.stackid, 
		a.segment, 
		' else '' end || ' 
		a.poll,
		''Warning''::character varying(11) as status,
		null::character varying(255) as control_program,
		case 
			when a.poll = ''PM10'' and a.ceff is not null then 
				''PM2_5 is missing CEFF, '' || a.ceff || ''%.''
			when a.poll = ''PM2_5'' and a.ceff is null then 
				''PM2_5 is missing CEFF, '' || b.ceff || ''%.''
			when a.poll = ''PM2_5'' and a.ceff is not null then 
				''PM10 is missing CEFF, '' || a.ceff || ''%.''
			when a.poll = ''PM10'' and a.ceff is null then 
				''PM10 is missing CEFF, '' || b.ceff || ''%.''
			else ''''
			end  as "comment"
	FROM emissions.' || inv_table_name || ' a

		full join emissions.' || inv_table_name || ' b
		on b.fips = a.fips
		and b.scc = a.scc

		' || case when is_point_table then '
		and b.plantid = a.plantid
		and b.pointid = a.pointid
		and b.stackid = a.stackid
		and b.segment = a.segment
		' else '' end || ' 
	where ' || inv_filter || ' and b.poll in (''PM2_5'',''PM10'')
		and a.poll in (''PM2_5'',''PM10'')
		and (
			(b.ceff is null and a.ceff is not null)
			or (b.ceff is not null and a.ceff is null)
		)
	order by a.fips,
		a.scc, 
		' || case when is_point_table then '
		a.plantid, 
		a.pointid, 
		a.stackid, 
		a.segment, 
		' else '' end || ' 
		a.poll';

END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;