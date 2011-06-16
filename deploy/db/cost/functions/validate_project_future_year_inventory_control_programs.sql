--select public.validate_project_future_year_inventory_control_programs(137, 6030);

CREATE OR REPLACE FUNCTION public.validate_project_future_year_inventory_control_programs(
	control_strategy_id integer,
	message_strategy_result_id integer
	) RETURNS void AS
$BODY$
DECLARE
	inventory_record RECORD;
	control_program RECORD;
	count integer := 0;
	sql character varying := '';
	control_program_dataset_filter_sql character varying := '';


	inv_table_name varchar(64) := '';
	inv_filter text := '';
	inv_fips_filter text := '';
	county_dataset_id integer := null;
	county_dataset_version integer := null;
	county_dataset_filter_sql text := '';
	inventory_year int;
	compliance_date_cutoff_daymonth varchar(256) := '';
	effective_date_cutoff_daymonth varchar(256) := '';

	strategy_messages_dataset_id integer := null;
	strategy_messages_table_name varchar(64) := '';
	prev_cp varchar(64) := '';
	has_error boolean := false;
BEGIN

	-- get the detailed result dataset info
	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
	where sr.id = message_strategy_result_id
	into strategy_messages_dataset_id,
		strategy_messages_table_name;

	SELECT case when length(trim(cs.filter)) > 0 then '(' || cs.filter || ')' else null end,
		cs.county_dataset_id,
		cs.county_dataset_version,
		cs.analysis_year
	FROM emf.control_strategies cs
	where cs.id = control_strategy_id
	INTO inv_filter,
		county_dataset_id,
		county_dataset_version,
		inventory_year;

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
		county_dataset_filter_sql := ' and inv.fips in (SELECT inv.fips
			FROM emissions.' || (SELECT table_name FROM emf.internal_sources where dataset_id = county_dataset_id) || ' inv
			where ' || public.build_version_where_filter(county_dataset_id, county_dataset_version, 'inv') || ')';
	END IF;

	-- validate that all inputs look fine, before actually running the projection.

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
		and cpt."name" = ''Plant Closure''
		order by processing_order'
	LOOP
		-- look at the closure control program inputs and table format (make sure all the right columns are in the table)
		IF control_program.type = 'Plant Closure' THEN

			IF not public.check_table_for_columns(control_program.table_name, 'effective_date,fips,plantid,pointid,stackid,segment,plant', ',') THEN
			--	RAISE EXCEPTION 'Control program, %, plant closure dataset table has incorrect table structure, expecting the following columns -- fips, plantid, pointid, stackid, segment, plant, and effective_date.', control_program.control_program_name;

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
					message_type,
					message,
					inventory
					)
				select 
					' || strategy_messages_dataset_id || '::integer,
					null::character varying(6) as fips, 
					null::character varying(10) as scc, 
					null::character varying(15) as plantid, 
					null::character varying(15) as pointid, 
					null::character varying(15) as stackid, 
					null::character varying(15) as segment, 
					null::character varying(16) as poll, 
					''Error''::character varying(11) as status,
					''' || control_program.control_program_name || '''::character varying(255) as control_program,
					''Packet Level''::character varying(255) as message_type,
					''Control program, ' || control_program.control_program_name || ', plant closure dataset table has incorrect table structure, expecting the following columns -- fips, plantid, pointid, stackid, segment, plant, and effective_date.'' as "message",
					null::character varying(255) as inventory';
				has_error := true;
			END IF;

			-- make sure the plant closure effective date is in the right format
			execute 'select count(1) from emissions.' || control_program.table_name || ' where not public.isdate(effective_date) and ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version)
			into count;
			IF count > 0 THEN
				--RAISE EXCEPTION 'Control program, %, plant closure dataset has % effective date(s) that are not in the correct date format.', control_program.control_program_name, count;
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
					message_type,
					message,
					inventory
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
					''Error''::character varying(11) as status,
					''' || control_program.control_program_name || '''::character varying(255) as control_program,
					''Packet Level''::character varying(255) as message_type,
					''Control program, ' || control_program.control_program_name || ', plant closure dataset has ' || count || ' effective date(s) that are not in the correct date format.'' as "message",
					null::character varying(255) as inventory
				from emissions.' || control_program.table_name || ' 
				where not public.isdate(effective_date) 
					and ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version);
				has_error := true;
			END IF;

			-- make sure there aren't missing fips codes
			execute 'select count(1) from emissions.' || control_program.table_name || ' where coalesce(trim(fips), '''') = ''''' || '  and ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version)
			into count;
			IF count > 0 THEN
--				RAISE EXCEPTION 'Control program, %, plant closure dataset has % missing fip(s) codes.', control_program.control_program_name, count;
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
					message_type,
					message,
					inventory
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
					''Error''::character varying(11) as status,
					''' || control_program.control_program_name || '''::character varying(255) as control_program,
					''Packet Level''::character varying(255) as message_type,
					''Control program, ' || control_program.control_program_name || ', plant closure dataset has ' || count || ' missing fip(s) codes.'' as "message",
					null::character varying(255) as inventory
				from emissions.' || control_program.table_name || ' 
				where coalesce(trim(fips), '''') = ''''' || ' 
					and ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version);
				has_error := true;
			END IF;

			-- make sure there aren't missing plant ids
			execute 'select count(1) from emissions.' || control_program.table_name || ' where coalesce(trim(plantid), '''') = ''''' || '  and ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version)
			into count;
			IF count > 0 THEN
--				RAISE EXCEPTION 'Control program, %, plant closure dataset has % missing plant Id(s).', control_program.control_program_name, count;
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
					message_type,
					message,
					inventory
					)
				select 
					' || strategy_messages_dataset_id || '::integer,
					scc, 
					plantid, 
					pointid, 
					stackid, 
					segment, 
					poll, 
					''Error''::character varying(11) as status,
					''' || control_program.control_program_name || '''::character varying(255) as control_program,
					''Packet Level''::character varying(255) as message_type,
					''Control program, ' || control_program.control_program_name || ', plant closure dataset has ' || count || ' missing plant Id(s).'' as "message",
					null::character varying(255) as inventory
				from emissions.' || control_program.table_name || ' 
				where coalesce(trim(plantid), '''') = ''''' || ' 
					and ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version);
				has_error := true;
			END IF;

		-- look at the projection control program table format (make sure all the right columns are in the table)
		ELSIF control_program.type = 'Projection' THEN

			-- currently no validation is required...

		END IF;
		
	
	END LOOP;

	-- look at the control packet control program, make sure there are no duplicates maching between an additonal and replacement control.

	-- TODO Possibly:  did something similar for ALLOWABLE Packets
/*	FOR inventory_record IN EXECUTE 
		'select lower(i.table_name) as table_name, 
			inv.dataset_id, 
			inv.dataset_version,
			d.name as dataset_name
		 from emf.input_datasets_control_strategies inv

			inner join emf.internal_sources i
			on i.dataset_id = inv.dataset_id

			inner join emf.datasets d
			on d.id = inv.dataset_id

		where inv.control_strategy_id = ' || control_strategy_id
	LOOP
*/		-- reset
		sql := '';
		
		-- build version info into where clause filter
		--inv_filter := '(' || public.build_version_where_filter(inventory_record.dataset_id, inventory_record.dataset_version, 'inv') || ')' || coalesce(' and ' || inv_filter, '');

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
				and cpt."name" in ( ''Control'',''Projection'',''Allowable'')
			order by cpt."name"'
			
		LOOP

			If prev_cp <> control_program.dataset_type THEN
		IF length(sql) > 0 THEN
			--raise notice '%',
			execute 
			'select count(1)
				from (' || sql || ') tbl
				group by fips,scc,plantid,pointid,stackid,segment,poll,sic,mact,naics,compliance_date
				having count(1) >= 2
				limit 1'
			into count;
			IF count > 0 THEN
--				RAISE EXCEPTION 'Inventory, %, has source(s) with duplicate matching hierarchy packet records.  See the Strategy Messages output dataset for more detailed information on identifying the matching hierarchy records.', inventory_record.dataset_name;



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
					message_type,
					message,
					inventory,
					sic,
					mact,
					naics,
					--replacement,
					compliance_date
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
					''Error''::character varying(11) as status,
					null::character varying(255) as control_program,
					''Packet Level''::character varying(255) as message_type,
					''Control programs, '' || string_agg(distinct control_program_name || '' ['' || packet_record_count || '' record'' || case when packet_record_count = 1 then '''' else ''s'' end || '']'', '','' order by control_program_name || '' ['' || packet_record_count || '' record'' || case when packet_record_count = 1 then '''' else ''s'' end || '']'') || '', have duplicate matching hierarchy packet records.'' as "message",
					null::character varying(255) as inventory,
					sic,
					mact,
					naics,
					--replacement,
					compliance_date
				from (

			select *, count(1) OVER w as packet_record_count
			from (' || sql || ') tbl
			WINDOW w AS (PARTITION BY fips,scc,plantid,pointid,stackid,segment,poll,sic,naics,mact,compliance_date,control_program_name)


				) tbl
				group by fips,scc,plantid,pointid,stackid,segment,poll,sic,naics,mact,compliance_date
				having count(1) >= 2
				';
				has_error := true;
			END IF;

		END IF;
				sql := '';
			END IF;
		
			If length(sql) > 0 THEN 
				sql := sql || ' union all ';
			END IF;

			--see http://www.smoke-model.org/version2.4/html/ch06s02.html for source matching hierarchy


-- TODO:  Get rid of distinct and see if this catches dups in the same packet...not sure current approach will catch this issue
			sql := sql || '
			select 
			--distinct on (record_id)
				fips,scc,plantid,pointid,stackid,segment,poll,sic,
			' || case 
				when control_program.type = 'Control' then 
				'mact,null::character varying(6) as naics,compliance_date,'
				when control_program.type = 'Allowable' then 
				'null::character varying(6) as mact,naics,compliance_date,'
				when control_program.type = 'Projection' then 
				'mact,naics,null::timestamp without time zone as compliance_date,'
				else 
					' '
			end || '
				' || quote_literal(control_program.control_program_name) || '::character varying(255) as control_program_name
			from emissions.' || control_program.table_name || '
			where 
			' || case 
				when control_program.type = 'Control' then 
					'coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone 
					and application_control = ''Y'' and '
				when control_program.type = 'Allowable' then 
					'coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone and ' 
				else 
					' '
			end || ' ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version) ||'';
			prev_cp := control_program.dataset_type;
		END LOOP;



		IF length(sql) > 0 THEN
			--raise notice '%',
			execute 
			'select count(1)
				from (' || sql || ') tbl
				group by fips,scc,plantid,pointid,stackid,segment,poll,sic,mact,naics,compliance_date
				having count(1) >= 2
				limit 1'
			into count;
			IF count > 0 THEN
--				RAISE EXCEPTION 'Inventory, %, has source(s) with duplicate matching hierarchy packet records.  See the Strategy Messages output dataset for more detailed information on identifying the matching hierarchy records.', inventory_record.dataset_name;



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
					message_type,
					message,
					inventory,
					sic,
					mact,
					naics,
					--replacement,
					compliance_date
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
					''Error''::character varying(11) as status,
					null::character varying(255) as control_program,
					''Packet Level''::character varying(255) as message_type,
					''Control programs, '' || string_agg(distinct control_program_name || '' ['' || packet_record_count || '' record'' || case when packet_record_count = 1 then '''' else ''s'' end || '']'', '','' order by control_program_name || '' ['' || packet_record_count || '' record'' || case when packet_record_count = 1 then '''' else ''s'' end || '']'') || '', have duplicate matching hierarchy packet records.'' as "message",
					null::character varying(255) as inventory,
					sic,
					mact,
					naics,
					--replacement,
					compliance_date
				from (

			select *, count(1) OVER w as packet_record_count
			from (' || sql || ') tbl
			WINDOW w AS (PARTITION BY fips,scc,plantid,pointid,stackid,segment,poll,sic,naics,mact,compliance_date,control_program_name)


				) tbl
				group by fips,scc,plantid,pointid,stackid,segment,poll,sic,naics,mact,compliance_date
				having count(1) >= 2
				';
				has_error := true;
			END IF;

		END IF;

/*
	END LOOP;
*/
--				where replacement in (''A'',''R'')
--				having count(1) = 2
--				group by record_id, ranking

END;
$BODY$
  LANGUAGE 'plpgsql';
ALTER FUNCTION public.validate_project_future_year_inventory_control_programs(int) OWNER TO emf;

