--SELECT public.populate_project_future_year_inventory_strategy_messages(89, 952, 0, 3135)

--DROP FUNCTION public.populate_project_future_year_inventory_strategy_messages(integer, integer, integer, integer);

CREATE OR REPLACE FUNCTION public.populate_project_future_year_inventory_strategy_messages(
	int_control_strategy_id integer, 
	input_dataset_id integer, 
	input_dataset_version integer, 
	strategy_result_id integer
	) RETURNS void AS
$BODY$
DECLARE
	inv_table_name varchar(64) := '';
	inv_filter text := '';
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
	compliance_date_cutoff_daymonth varchar(256) := '';
	effective_date_cutoff_daymonth varchar(256) := '';
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
	SELECT cs.pollutant_id,
		case when length(trim(cs.filter)) > 0 then '(' || public.alias_inventory_filter(cs.filter, 'inv') || ')' else null end,
		cs.cost_year,
		cs.analysis_year,
		cs.county_dataset_id,
		cs.county_dataset_version,
		cs.use_cost_equations,
		cs.discount_rate / 100
	FROM emf.control_strategies cs
	where cs.id = int_control_strategy_id
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
	where csc.control_strategy_id = int_control_strategy_id
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

	-- load the Compliance and Effective Date Cutoff Day/Month (Stored as properties)
	select value
	from emf.properties
	where "name" = 'COST_PROJECT_FUTURE_YEAR_COMPLIANCE_DATE_CUTOFF_MONTHDAY'
	into compliance_date_cutoff_daymonth;
	compliance_date_cutoff_daymonth := coalesce(compliance_date_cutoff_daymonth, '07/01');	--default just in case
	select value
	from emf.properties
	where "name" = 'COST_PROJECT_FUTURE_YEAR_EFFECTIVE_DATE_CUTOFF_MONTHDAY'
	into effective_date_cutoff_daymonth;
	effective_date_cutoff_daymonth := coalesce(effective_date_cutoff_daymonth, '07/01');	--default just in case
	
	-- see if their was a county dataset specified for the strategy, is so then build a sql where clause filter for later use
	IF county_dataset_id is not null THEN
		county_dataset_filter_sql := ' and inv.fips in (SELECT fips
			FROM emissions.' || (SELECT table_name FROM emf.internal_sources where dataset_id = county_dataset_id) || '
			where ' || public.build_version_where_filter(county_dataset_id, county_dataset_version) || ')';
	END IF;
	-- build version info into where clause filter
	inv_filter := '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'inv') || ')' || coalesce(' and ' || inv_filter, '');


	--give warning if CE is 100 and was assumed to be 0
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
		inv.fips,
		inv.scc,
		' || case when is_point_table then '
		inv.plantid, 
		inv.pointid, 
		inv.stackid, 
		inv.segment, 
		' else '
		null::character varying(15) as plantid, 
		null::character varying(15) as pointid, 
		null::character varying(15) as stackid, 
		null::character varying(15) as segment, 
		' end || '
		inv.poll,
		''Warning''::character varying(11) as status,
		''None''::character varying(255) as control_program,
		''Source has a 100 ceff but has an emission.'' as "comment"
	FROM emissions.' || inv_table_name || ' inv
	where 	inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) <> 0.0'
	|| ' and ' || inv_filter;

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
		where csp.control_strategy_id = ' || int_control_strategy_id || '
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

		-- see if there any matching issues with the projection, control, or allowable packet
		ELSIF control_program.type = 'Projection' or control_program.type = 'Control' or control_program.type = 'Allowable' THEN

			-- make sure the dataset type is right...
			IF (control_program.type = 'Projection' and control_program.dataset_type = 'Projection Packet') 
				or (control_program.type = 'Control' and control_program.dataset_type = 'Control Packet')  
				or (control_program.type = 'Allowable' and control_program.dataset_type = 'Allowable Packet') THEN
				--store control dataset version filter in variable
				select public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'packet')
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
					--1 - Country/State/County code, plant ID, point ID, stack ID, segment, 8-digit SCC code, pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.fips = inv.fips
						and packet.plantid = inv.plantid
						and packet.pointid = inv.pointid
						and packet.stackid = inv.stackid
						and packet.segment = inv.segment
						and packet.scc = inv.scc
						and packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is not null 
						and packet.pointid is not null 
						and packet.stackid is not null 
						and packet.segment is not null 
						and packet.scc is not null 
						and packet.poll is not null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '

						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--2 - Country/State/County code, plant ID, point ID, stack ID, segment, pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.fips = inv.fips
						
						and packet.plantid = inv.plantid
						and packet.pointid = inv.pointid
						and packet.stackid = inv.stackid
						and packet.segment = inv.segment
						and packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is not null 
						and packet.pointid is not null 
						and packet.stackid is not null 
						and packet.segment is not null 
						and packet.scc is null 
						and packet.poll is not null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--3 - Country/State/County code, plant ID, point ID, stack ID, pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.fips = inv.fips
						
						and packet.plantid = inv.plantid
						and packet.pointid = inv.pointid
						and packet.stackid = inv.stackid
						and packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is not null 
						and packet.pointid is not null 
						and packet.stackid is not null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is not null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--4 - Country/State/County code, plant ID, point ID, pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.fips = inv.fips
						
						and packet.plantid = inv.plantid
						and packet.pointid = inv.pointid
						and packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is not null 
						and packet.pointid is not null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is not null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--5 - Country/State/County code, plant ID, 8-digit SCC code, pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.fips = inv.fips
						
						and packet.plantid = inv.plantid
						and packet.scc = inv.scc
						and packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is not null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is not null 
						and packet.poll is not null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					' || case when has_mact_column and control_program.type <> 'Allowable' then '
					--5.5 - Country/State/County code, plant ID, MACT code, pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records. mact = '' || packet.mact as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.fips = inv.fips
						
						and packet.plantid = inv.plantid
						and packet.mact = inv.mact
						and packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is not null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is not null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is not null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null
					' else '' end || '

					--6 - Country/State/County code, plant ID, pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.fips = inv.fips
						
						and packet.plantid = inv.plantid
						and packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is not null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is not null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--7 - Country/State/County code, plant ID, point ID, stack ID, segment, 8-digit SCC code
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.fips = inv.fips
						
						and packet.plantid = inv.plantid
						and packet.pointid = inv.pointid
						and packet.stackid = inv.stackid
						and packet.segment = inv.segment
						and packet.scc = inv.scc
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is not null 
						and packet.pointid is not null 
						and packet.stackid is not null 
						and packet.segment is not null 
						and packet.scc is not null 
						and packet.poll is null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--8- Country/State/County code, plant ID, point ID, stack ID, segment
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.fips = inv.fips
						
						and packet.plantid = inv.plantid
						and packet.pointid = inv.pointid
						and packet.stackid = inv.stackid
						and packet.segment = inv.segment
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is not null 
						and packet.pointid is not null 
						and packet.stackid is not null 
						and packet.segment is not null 
						and packet.scc is null 
						and packet.poll is null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--9 - Country/State/County code, plant ID, point ID, stack ID
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.fips = inv.fips
						
						and packet.plantid = inv.plantid
						and packet.pointid = inv.pointid
						and packet.stackid = inv.stackid
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is not null 
						and packet.pointid is not null 
						and packet.stackid is not null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--10 - Country/State/County code, plant ID, point id
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.fips = inv.fips
						
						and packet.plantid = inv.plantid
						and packet.pointid = inv.pointid
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is not null 
						and packet.pointid is not null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--11 - Country/State/County code, plant ID, 8-digit SCC code
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.fips = inv.fips
						
						and packet.plantid = inv.plantid
						and packet.scc = inv.scc
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is not null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is not null 
						and packet.poll is null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					' || case when has_mact_column and control_program.type <> 'Allowable' then '
					--12 - Country/State/County code, plant ID, MACT code
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records. mact = '' || packet.mact as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.fips = inv.fips
						
						and packet.plantid = inv.plantid
						and packet.mact = inv.mact
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is not null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is not null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null
					' else '' end || '

					--13 - Country/State/County code, plant ID
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.fips = inv.fips
						
						and packet.plantid = inv.plantid
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is not null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null
					' else '' end || '
					
					' || case when has_mact_column and control_program.type <> 'Allowable' then '
					--14,16 - Country/State/County code or Country/State code, MACT code, 8-digit SCC code, pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records. mact = '' || packet.mact as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on (packet.fips = inv.fips or packet.fips = substr(inv.fips, 1, 2) or (substr(packet.fips,3,3) = ''000'' and substr(packet.fips, 1, 2) = substr(inv.fips, 1, 2)))
						
						and packet.mact = inv.mact
						and packet.scc = inv.scc
						and packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is not null 
						and packet.poll is not null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is not null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--15,17 - Country/State/County code or Country/State code, MACT code, pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records. mact = '' || packet.mact as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on (packet.fips = inv.fips or packet.fips = substr(inv.fips, 1, 2) or (substr(packet.fips,3,3) = ''000'' and substr(packet.fips, 1, 2) = substr(inv.fips, 1, 2)))
						
						and packet.mact = inv.mact
						and packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is not null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is not null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--18 - MACT code, 8-digit SCC code, pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records. mact = '' || packet.mact as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.mact = inv.mact
						and packet.scc = inv.scc
						and packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is not null 
						and packet.poll is not null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is not null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--19 - MACT code, pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records. mact = '' || packet.mact as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.mact = inv.mact
						and packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is not null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is not null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--20,22 - Country/State/County code or Country/State code, 8-digit SCC code, MACT code
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records. mact = '' || packet.mact as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on (packet.fips = inv.fips or packet.fips = substr(inv.fips, 1, 2) or (substr(packet.fips,3,3) = ''000'' and substr(packet.fips, 1, 2) = substr(inv.fips, 1, 2)))
						
						and packet.mact = inv.mact
						and packet.scc = inv.scc
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is not null 
						and packet.poll is null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is not null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--21,23 - Country/State/County code or Country/State code, MACT code
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records. mact = '' || packet.mact as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on (packet.fips = inv.fips or packet.fips = substr(inv.fips, 1, 2) or (substr(packet.fips,3,3) = ''000'' and substr(packet.fips, 1, 2) = substr(inv.fips, 1, 2)))
						
						and packet.mact = inv.mact
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is not null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--24 - MACT code, 8-digit SCC code
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records. mact = '' || packet.mact as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.mact = inv.mact
						and packet.scc = inv.scc
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is not null 
						and packet.poll is null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is not null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--25 - MACT code
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records. mact = '' || packet.mact as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.mact = inv.mact
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is not null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null
					' else '' end || '

					' || case when has_sic_column then '
					--25.5,27.5 - Country/State/County code or Country/State code, 8-digit SCC code, 4-digit SIC code, pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records. sic = '' || packet.sic as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on (packet.fips = inv.fips or packet.fips = substr(inv.fips, 1, 2) or (substr(packet.fips,3,3) = ''000'' and substr(packet.fips, 1, 2) = substr(inv.fips, 1, 2)))
						and packet.sic = inv.sic
						and packet.scc = inv.scc
						and packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is not null 
						and packet.poll is not null
						and packet.sic is not null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--26,28 - Country/State/County code or Country/State code, 4-digit SIC code, pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records. sic = '' || packet.sic as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on (packet.fips = inv.fips or packet.fips = substr(inv.fips, 1, 2) or (substr(packet.fips,3,3) = ''000'' and substr(packet.fips, 1, 2) = substr(inv.fips, 1, 2)))
						and packet.sic = inv.sic
						and packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is not null
						and packet.sic is not null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--29.5 - 4-digit SIC code, SCC code, pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records. sic = '' || packet.sic as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.sic = inv.sic
						and packet.scc = inv.scc
						and packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is not null 
						and packet.poll is not null
						and packet.sic is not null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--30 - 4-digit SIC code, pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records. sic = '' || packet.sic as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.sic = inv.sic
						and packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is not null
						and packet.sic is not null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--31.5,33.5 - Country/State/County code or Country/State code, 4-digit SIC code, SCC code
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records. sic = '' || packet.sic as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on (packet.fips = inv.fips or packet.fips = substr(inv.fips, 1, 2) or (substr(packet.fips,3,3) = ''000'' and substr(packet.fips, 1, 2) = substr(inv.fips, 1, 2)))
						and packet.sic = inv.sic
						and packet.scc = inv.scc
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is not null 
						and packet.poll is null
						and packet.sic is not null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--32,34 - Country/State/County code or Country/State code, 4-digit SIC code
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records. sic = '' || packet.sic as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on (packet.fips = inv.fips or packet.fips = substr(inv.fips, 1, 2) or (substr(packet.fips,3,3) = ''000'' and substr(packet.fips, 1, 2) = substr(inv.fips, 1, 2)))
						and packet.sic = inv.sic
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is null
						and packet.sic is not null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--35.5 - 4-digit SIC code, SCC code
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records. sic = '' || packet.sic as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.sic = inv.sic
						and packet.scc = inv.scc
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is not null 
						and packet.poll is null
						and packet.sic is not null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--36 - 4-digit SIC code
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records. sic = '' || packet.sic as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.sic = inv.sic
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is null
						and packet.sic is not null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null
					' else '' end || '

					--38,42 - Country/State/County code or Country/State code, 8-digit SCC code, pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on (packet.fips = inv.fips or packet.fips = substr(inv.fips, 1, 2) or (substr(packet.fips,3,3) = ''000'' and substr(packet.fips, 1, 2) = substr(inv.fips, 1, 2)))
						and packet.scc = inv.scc
						and packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is not null 
						and packet.poll is not null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--46 - 8-digit SCC code, pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.scc = inv.scc
						and packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is not null 
						and packet.poll is not null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--50,54 - Country/State/County code or Country/State code, 8-digit SCC code
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on (packet.fips = inv.fips or packet.fips = substr(inv.fips, 1, 2) or (substr(packet.fips,3,3) = ''000'' and substr(packet.fips, 1, 2) = substr(inv.fips, 1, 2)))
						and packet.scc = inv.scc
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is not null 
						and packet.poll is null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--58 - 8-digit SCC code
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.scc = inv.scc
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is not null 
						and packet.poll is null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--62,64 - Country/State/County code or Country/State code, pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on (packet.fips = inv.fips or packet.fips = substr(inv.fips, 1, 2) or (substr(packet.fips,3,3) = ''000'' and substr(packet.fips, 1, 2) = substr(inv.fips, 1, 2)))
						and packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is not null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--63,65 - Country/State/County code or Country/State code
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on (packet.fips = inv.fips or packet.fips = substr(inv.fips, 1, 2) or (substr(packet.fips,3,3) = ''000'' and substr(packet.fips, 1, 2) = substr(inv.fips, 1, 2)))
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is not null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null

					--66 - Pollutant
					union all
					select 
						packet.fips,packet.plantid,packet.pointid,packet.stackid,packet.segment,packet.scc,packet.poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message
					FROM emissions.' || control_program.table_name || ' packet
						left outer join emissions.' || inv_table_name || ' inv
								
						on packet.poll = inv.poll
						and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

					where packet.fips is null 
						and packet.plantid is null 
						and packet.pointid is null 
						and packet.stackid is null 
						and packet.segment is null 
						and packet.scc is null 
						and packet.poll is not null
						and packet.sic is null
						' || case when control_program.type <> 'Allowable' then 'and packet.mact is null' else '' end || '
						and ' || control_program_dataset_filter_sql || '
						' || case when control_program.type <> 'Projection' then '
						-- make the compliance date has been met
						and coalesce(packet.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						' else '' end || '
						and inv.record_id is null
				) tbl';

			END IF;
		END IF;



	END LOOP;



END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;