--select public.validate_project_future_year_inventory_control_programs(109);

CREATE OR REPLACE FUNCTION public.validate_project_future_year_inventory_control_programs(
	control_strategy_id integer) RETURNS void AS
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
BEGIN

	SELECT case when length(trim(cs.filter)) > 0 then '(' || public.alias_inventory_filter(cs.filter, 'inv') || ')' else null end,
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
		county_dataset_filter_sql := ' and inv.fips in (SELECT fips
			FROM emissions.' || (SELECT table_name FROM emf.internal_sources where dataset_id = county_dataset_id) || '
			where ' || public.build_version_where_filter(county_dataset_id, county_dataset_version) || ')';
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
				RAISE EXCEPTION 'Control program, %, plant closure dataset table has incorrect table structure, expecting the following columns -- fips, plantid, pointid, stackid, segment, plant, and effective_date.', control_program.control_program_name;
				return;
			END IF;

			-- make sure the plant closure effective date is in the right format
			execute 'select count(1) from emissions.' || control_program.table_name || ' where not public.isdate(effective_date) and ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version)
			into count;
			IF count > 0 THEN
				RAISE EXCEPTION 'Control program, %, plant closure dataset has % effective date(s) that are not in the correct date format.', control_program.control_program_name, count;
				return;
			END IF;

			-- make sure there aren't missing fips codes
			execute 'select count(1) from emissions.' || control_program.table_name || ' where coalesce(trim(fips), '''') = ''''' || '  and ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version)
			into count;
			IF count > 0 THEN
				RAISE EXCEPTION 'Control program, %, plant closure dataset has % missing fip(s) codes.', control_program.control_program_name, count;
				return;
			END IF;

			-- make sure there aren't missing plant ids
			execute 'select count(1) from emissions.' || control_program.table_name || ' where coalesce(trim(plantid), '''') = ''''' || '  and ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version)
			into count;
			IF count > 0 THEN
				RAISE EXCEPTION 'Control program, %, plant closure dataset has % missing plant Id(s).', control_program.control_program_name, count;
				return;
			END IF;

		-- look at the projection control program table format (make sure all the right columns are in the table)
		ELSIF control_program.type = 'Projection' THEN

			-- currently no validation is required...

		END IF;
		
	
	END LOOP;

	-- look at the control packet control program, make sure there are no duplicates maching between an additonal and replacement control.

	FOR inventory_record IN EXECUTE 
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
		-- reset
		sql := '';
		
		-- build version info into where clause filter
		inv_filter := '(' || public.build_version_where_filter(inventory_record.dataset_id, inventory_record.dataset_version, 'inv') || ')' || coalesce(' and ' || inv_filter, '');

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
			and cpt."name" = ''Control'''
		LOOP

			--store control dataset version filter in variable
			select public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'proj')
			into control_program_dataset_filter_sql;
			
			If length(sql) > 0 THEN 
				sql := sql || ' union all ';
			END IF;

			--see http://www.smoke-model.org/version2.4/html/ch06s02.html for source matching hierarchy
			sql := sql || '
			select distinct on (record_id)
				record_id,fips,scc,plantid,pointid,stackid,segment,poll,sic,mact,replacement,compliance_date,
				' || quote_literal(control_program.control_program_name) || ' as control_program_name,
				ranking
			from (
				--placeholder helps dealing with point vs non-point inventories, i dont have to worry about the union all statements
				select 
					null::integer as record_id, null::character varying(6) as fips,null::character varying(10) as scc,null::character varying(15) as plantid,null::character varying(15) as pointid,null::character varying(15) as stackid,null::character varying(15) as segment,null::character varying(16) as poll,null::character varying(4) as sic,null::character varying(6) as mact,
					null::character varying(1) as replacement, null::timestamp without time zone as compliance_date, null::integer as ranking
				where 1 = 0

				' || public.build_project_future_year_inventory_matching_hierarchy_sql(
					control_program.table_name, 
					inventory_record.table_name, 
					'proj.fips,proj.scc,proj.plantid,proj.pointid,proj.stackid,proj.segment,proj.poll,proj.sic,proj.mact,proj.replacement,proj.compliance_date,',
					control_program_dataset_filter_sql || ' 
					and proj.application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || ' 
					and coalesce(proj.compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone'
					) || '
			) tbl';


		END LOOP;


		IF length(sql) > 0 THEN
			execute 'select count(1)
				from (' || sql || ') tbl
				group by record_id,fips,scc,plantid,pointid,stackid,segment,poll,sic,mact,ranking,compliance_date
				having count(1) >= 2
				limit 1'
			into count;
			IF count > 0 THEN
				RAISE EXCEPTION 'Inventory, %, dataset has source(s) with duplicate matching hierarchy packet records.  See the Strategy Messages output dataset for more detailed information on identifying the matching hierarchy records.', inventory_record.dataset_name;
				return;
			END IF;

		END IF;

	END LOOP;
--				where replacement in (''A'',''R'')
--				having count(1) = 2
--				group by record_id, ranking

END;
$BODY$
  LANGUAGE 'plpgsql' IMMUTABLE;
ALTER FUNCTION public.validate_project_future_year_inventory_control_programs(int) OWNER TO emf;

