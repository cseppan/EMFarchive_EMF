CREATE OR REPLACE FUNCTION public.validate_project_future_year_inventory_control_programs(
	control_strategy_id integer) RETURNS void AS
$BODY$
DECLARE
	control_program RECORD;
	count integer := 0;
BEGIN

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
		order by processing_order'
	LOOP
		-- look at the closure control program inputs and table format (make sure all the right columns are in the table)
		IF control_program.type = 'Plant Closure' THEN

			IF not public.check_table_for_columns(control_program.table_name, 'effective_date,fips,plantid,pointid,stackid,segment,plant', ',') THEN
				RAISE EXCEPTION 'Control program, %, plant closure dataset table has incorrect table structure, expecting the following columns -- fips, plantid, pointid, stackid, segment, plant, and effective_date.', control_program.control_program_name;
				return;
			END IF;

			-- make sure the plant closure effective date is in the right format
			execute 'select count(1) from emissions.' || control_program.table_name || ' where not public.isdate(effective_date)'
			into count;
			IF count > 0 THEN
				RAISE EXCEPTION 'Control program, %, plant closure dataset has % effective date(s) that are not in the correct date format.', control_program.control_program_name, count;
				return;
			END IF;

			-- make sure there aren't missing fips codes
			execute 'select count(1) from emissions.' || control_program.table_name || ' where coalesce(trim(fips), '''') = ''''' || ' '
			into count;
			IF count > 0 THEN
				RAISE EXCEPTION 'Control program, %, plant closure dataset has % missing fip(s) codes.', control_program.control_program_name, count;
				return;
			END IF;

			-- make sure there aren't missing plant ids
			execute 'select count(1) from emissions.' || control_program.table_name || ' where coalesce(trim(plantid), '''') = ''''' || ' '
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

END;
$BODY$
  LANGUAGE 'plpgsql' IMMUTABLE;
ALTER FUNCTION public.validate_project_future_year_inventory_control_programs(int) OWNER TO emf;

