--SELECT public.populate_project_future_year_inventory_strategy_messages(89, 952, 0, 3135)

CREATE OR REPLACE FUNCTION public.populate_project_future_year_inventory_strategy_messages(
	control_strategy_id integer, 
	input_dataset_id integer, 
	input_dataset_version integer, 
	strategy_result_id integer
	) RETURNS void AS
$BODY$
DECLARE
	inv_table_name varchar(64) := '';
	inv_filter varchar := '';
	inv_fips_filter text := '';
	strategy_messages_dataset_id integer := null;
	strategy_messages_table_name varchar(64) := '';
	county_dataset_id integer := null;
	county_dataset_version integer := null;
	region RECORD;
	control_program RECORD;
	target_pollutant_id integer := 0;
	measures_count integer := 0;
	measure_with_region_count integer := 0;
	measure_classes_count integer := 0;
	county_dataset_filter_sql text := '';
	control_program_dataset_filter_sql text := '';
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

	-- get the detailed result dataset info
	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
	where sr.id = strategy_result_id
	into strategy_messages_dataset_id,
		strategy_messages_table_name;

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
		county_dataset_filter_sql := ' and inv.fips in (SELECT fips
			FROM emissions.' || (SELECT table_name FROM emf.internal_sources where dataset_id = county_dataset_id) || '
			where ' || public.build_version_where_filter(county_dataset_id, county_dataset_version) || ')';
	END IF;
	-- build version info into where clause filter
	inv_filter := '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'inv') || ')' || coalesce(' and ' || inv_filter, '');

  	FOR control_program IN EXECUTE 
		'select cp."name" as control_program_name, cpt."name" as type, lower(i.table_name) as table_name, 
			cp.start_date, cp.end_date, 
			cp.dataset_id, cp.dataset_version,
			dt."name" as dataset_type
		 from emf.control_strategy_programs csp

			inner join emf.control_programs cp
			on cp.id = csp.control_program_id

			inner join emf.control_program_types cpt
			on cpt.id = cp.control_program_type_id

			inner join emf.internal_sources i
			on i.dataset_id = cp.dataset_id

			inner join emf.datasets d
			on d.id = i.dataset_id

			inner join emf.dataset_types dt
			on dt.id = d.dataset_type
		where csp.control_strategy_id = ' || control_strategy_id || '
		order by processing_order'
	LOOP

		-- see if there any issues with the plant closure file
		IF control_program.type = 'Plant Closure' THEN

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
				pc.fips,
				null::character varying(10) as scc,
				pc.plantid, 
				pc.pointid, 
				pc.stackid, 
				pc.segment, 
				null::character varying(16) as poll,
				''Warning''::character varying(11) as status,
				' || quote_literal(control_program.control_program_name) || '::character varying(255) as control_program,
				''Plant'' || case when pc.plant is not null and length(pc.plant) > 0 then coalesce('', '' || pc.plant || '','', '''') else '''' end || '' is missing from the inventory.'' as "comment"
			FROM emissions.' || control_program.table_name || ' pc

				left outer join emissions.' || inv_table_name || ' inv
				on pc.fips = inv.fips
				' || case when not is_point_table then '
				and pc.plantid is null
				and pc.pointid is null
				and pc.stackid is null
				and pc.segment is null
				' else '
				and pc.plantid = inv.plantid
				and coalesce(pc.pointid, inv.pointid) = inv.pointid
				and coalesce(pc.stackid, inv.stackid) = inv.stackid
				and coalesce(pc.segment, inv.segment) = inv.segment
				' end || '
				and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '


			where 	
				-- make sure and keep even if in the same year, they might not have closed...
				date_part(''year'', pc.effective_date::timestamp without time zone) < ' || inventory_year || '
				' || case when not is_point_table then '
--				and pc.plantid is null
--				and pc.pointid is null
--				and pc.stackid is null
--				and pc.segment is null
				' else '
				' end || '
				and ' || replace(replace(replace(public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version), 'version ', 'pc.version '), 'delete_versions ', 'pc.delete_versions '), 'dataset_id', 'pc.dataset_id') || '
				and inv.record_id is null';

		-- see if there any issues with the projection packet
		ELSIF control_program.type = 'Projection' THEN

			-- make sure the dataset type is right...
			IF control_program.dataset_type = 'Projection Packet' THEN
				--store control dataset version filter in variable
				select public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'proj')
				into control_program_dataset_filter_sql;
				
				--see http://www.smoke-model.org/version2.4/html/ch06s02.html for source matching hierarchy
--				raise notice '%', 
				execute
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
					fips, 
					scc, 
					plantid, 
					pointid, 
					stackid, 
					segment, 
					poll, 
					''Warning''::character varying(11) as status,
					' || quote_literal(control_program.control_program_name) || ' as control_program_name,
					message
					
				from (
					--placeholder helps dealing with point vs non-point inventories, i dont have to worry about the union all statements
					select 
						null::character varying as fips,null::character varying as plantid,null::character varying as pointid,null::character varying as stackid,null::character varying as segment,null::character varying as scc,null::character varying as poll,null::character varying as message
					where 1 = 0

					' || case when is_point_table then '
					--1
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.fips = inv.fips
						and proj.plantid = inv.plantid
						and proj.pointid = inv.pointid
						and proj.stackid = inv.stackid
						and proj.segment = inv.segment
						and proj.scc = inv.scc
						and proj.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is not null 
						and proj.pointid is not null 
						and proj.stackid is not null 
						and proj.segment is not null 
						and proj.scc is not null 
						and proj.poll is not null
						and proj.sic is null
						and proj.mact is null

						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--2
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.fips = inv.fips
						
						and proj.plantid = inv.plantid
						and proj.pointid = inv.pointid
						and proj.stackid = inv.stackid
						and proj.segment = inv.segment
						and proj.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is not null 
						and proj.pointid is not null 
						and proj.stackid is not null 
						and proj.segment is not null 
						and proj.scc is null 
						and proj.poll is not null
						and proj.sic is null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--3
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.fips = inv.fips
						
						and proj.plantid = inv.plantid
						and proj.pointid = inv.pointid
						and proj.stackid = inv.stackid
						and proj.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is not null 
						and proj.pointid is not null 
						and proj.stackid is not null 
						and proj.segment is null 
						and proj.scc is null 
						and proj.poll is not null
						and proj.sic is null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--4
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.fips = inv.fips
						
						and proj.plantid = inv.plantid
						and proj.pointid = inv.pointid
						and proj.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is not null 
						and proj.pointid is not null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is null 
						and proj.poll is not null
						and proj.sic is null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--5
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.fips = inv.fips
						
						and proj.plantid = inv.plantid
						and proj.scc = inv.scc
						and proj.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is not null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is not null 
						and proj.poll is not null
						and proj.sic is null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--6
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.fips = inv.fips
						
						and proj.plantid = inv.plantid
						and proj.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is not null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is null 
						and proj.poll is not null
						and proj.sic is null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--7
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.fips = inv.fips
						
						and proj.plantid = inv.plantid
						and proj.pointid = inv.pointid
						and proj.stackid = inv.stackid
						and proj.segment = inv.segment
						and proj.scc = inv.scc
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is not null 
						and proj.pointid is not null 
						and proj.stackid is not null 
						and proj.segment is not null 
						and proj.scc is not null 
						and proj.poll is null
						and proj.sic is null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--8
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.fips = inv.fips
						
						and proj.plantid = inv.plantid
						and proj.pointid = inv.pointid
						and proj.stackid = inv.stackid
						and proj.segment = inv.segment
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is not null 
						and proj.pointid is not null 
						and proj.stackid is not null 
						and proj.segment is not null 
						and proj.scc is null 
						and proj.poll is null
						and proj.sic is null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--9
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.fips = inv.fips
						
						and proj.plantid = inv.plantid
						and proj.pointid = inv.pointid
						and proj.stackid = inv.stackid
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is not null 
						and proj.pointid is not null 
						and proj.stackid is not null 
						and proj.segment is null 
						and proj.scc is null 
						and proj.poll is null
						and proj.sic is null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--10
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.fips = inv.fips
						
						and proj.plantid = inv.plantid
						and proj.pointid = inv.pointid
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is not null 
						and proj.pointid is not null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is null 
						and proj.poll is null
						and proj.sic is null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--11
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.fips = inv.fips
						
						and proj.plantid = inv.plantid
						and proj.scc = inv.scc
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is not null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is not null 
						and proj.poll is null
						and proj.sic is null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--12
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.fips = inv.fips
						
						and proj.plantid = inv.plantid
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is not null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is null 
						and proj.poll is null
						and proj.sic is null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null
					' else '' end || '
					
					' || case when has_mact_column then '
					--13,15
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records. mact = '' || proj.mact as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
						
						and proj.mact = inv.mact
						and proj.scc = inv.scc
						and proj.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is not null 
						and proj.poll is not null
						and proj.sic is null
						and proj.mact is not null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--14,16
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records. mact = '' || proj.mact as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
						
						and proj.mact = inv.mact
						and proj.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is null 
						and proj.poll is not null
						and proj.sic is null
						and proj.mact is not null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--17
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records. mact = '' || proj.mact as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.mact = inv.mact
						and proj.scc = inv.scc
						and proj.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is not null 
						and proj.poll is not null
						and proj.sic is null
						and proj.mact is not null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--18
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records. mact = '' || proj.mact as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.mact = inv.mact
						and proj.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is null 
						and proj.poll is not null
						and proj.sic is null
						and proj.mact is not null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--19,21
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records. mact = '' || proj.mact as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
						
						and proj.mact = inv.mact
						and proj.scc = inv.scc
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is not null 
						and proj.poll is null
						and proj.sic is null
						and proj.mact is not null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--20,22
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records. mact = '' || proj.mact as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
						
						and proj.mact = inv.mact
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is null 
						and proj.poll is null
						and proj.sic is null
						and proj.mact is not null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--23
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records. mact = '' || proj.mact as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.mact = inv.mact
						and proj.scc = inv.scc
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is not null 
						and proj.poll is null
						and proj.sic is null
						and proj.mact is not null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--24
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records. mact = '' || proj.mact as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.mact = inv.mact
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is null 
						and proj.poll is null
						and proj.sic is null
						and proj.mact is not null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null
					' else '' end || '

					' || case when has_sic_column then '
					--25,27
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records. sic = '' || proj.sic as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
						and proj.sic = inv.sic
						and proj.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is null 
						and proj.poll is not null
						and proj.sic is not null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--29
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records. sic = '' || proj.sic as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.sic = inv.sic
						and proj.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is null 
						and proj.poll is not null
						and proj.sic is not null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--31,33
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records. sic = '' || proj.sic as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
						and proj.sic = inv.sic
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is null 
						and proj.poll is null
						and proj.sic is not null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--35
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records. sic = '' || proj.sic as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.sic = inv.sic
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is null 
						and proj.poll is null
						and proj.sic is not null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null
					' else '' end || '

					--37,41
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
						and proj.scc = inv.scc
						and proj.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is not null 
						and proj.poll is not null
						and proj.sic is null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--45
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.scc = inv.scc
						and proj.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is not null 
						and proj.poll is not null
						and proj.sic is null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--49,53
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
						and proj.scc = inv.scc
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is not null 
						and proj.poll is null
						and proj.sic is null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--57
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.scc = inv.scc
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is not null 
						and proj.poll is null
						and proj.sic is null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--61,63
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
						and proj.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is null 
						and proj.poll is not null
						and proj.sic is null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--62,64
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is not null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is null 
						and proj.poll is null
						and proj.sic is null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null

					--65
					union all
					select 
						proj.fips,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.scc,proj.poll,''Projection packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' proj
						left outer join emissions.' || inv_table_name || ' inv
								
						on proj.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where proj.fips is null 
						and proj.plantid is null 
						and proj.pointid is null 
						and proj.stackid is null 
						and proj.segment is null 
						and proj.scc is null 
						and proj.poll is not null
						and proj.sic is null
						and proj.mact is null
						and ' || control_program_dataset_filter_sql || '
						and inv.record_id is null
				) tbl';

			END IF;
		END IF;


	END LOOP;



END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;