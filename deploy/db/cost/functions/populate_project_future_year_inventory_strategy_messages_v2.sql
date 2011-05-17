/*
truncate emissions.ds_ptinv_ptnonipm_2020cc_strategy_msgs_150848_20110314150848738 ;
SELECT public.populate_project_future_year_inventory_strategy_messages(137, 5454);
SELECT public.build_project_future_year_inventory_matching_hierarchy_sql(952, 0, 4120, 1, '', null, 2);
SELECT public.validate_project_future_year_inventory_control_programs(137);
SELECT public.populate_project_future_year_inventory_strategy_messages(137, 5583)
SELECT public.populate_project_future_year_inventory_strategy_messages(137, 5589)
select * from emissions.ds_ptinv_ptnonipm_2020cc_strategy_msgs_150848_20110314150848738
order by record_id desc
limit 1000;

select *
from emissions.DS_ptinv_ptnonipm_2020cc_1068478967 inv,
emissions.ds_deletions_2005_1050880615 pc
where pc.fips = inv.fips
and pc.plantid = inv.plantid
and coalesce(pc.pointid, inv.pointid) = inv.pointid
and coalesce(pc.stackid, inv.stackid) = inv.stackid
and coalesce(pc.segment, inv.segment) = inv.segment;



select 
					count(1) as cnt
				FROM emissions.ds_ptinv_ptnonipm_2020cc_1068478967 inv

					inner join emissions.ds_deletions_2005_1050880615 pc
					on pc.fips = inv.fips
					
					and pc.plantid = inv.plantid
					and coalesce(pc.pointid, inv.pointid) = inv.pointid
					and coalesce(pc.stackid, inv.stackid) = inv.stackid
					and coalesce(pc.segment, inv.segment) = inv.segment
					

					-- only keep if before cutoff date
					and coalesce(pc.effective_date, '1/1/1900')::timestamp without time zone < '07/01/2020'::timestamp without time zone

					and pc.version IN (0) and pc.dataset_id = 3877

				where 	(inv.version IN (0) and inv.dataset_id = 952) and (inv.fips like '37009')

*/


DROP FUNCTION IF EXISTS public.populate_project_future_year_inventory_strategy_messages(integer, integer);

CREATE OR REPLACE FUNCTION public.populate_project_future_year_inventory_strategy_messages(
	int_control_strategy_id integer, 
--	input_dataset_id integer, 
--	input_dataset_version integer, 
	strategy_result_id integer
	) RETURNS void AS
$BODY$
DECLARE
	inv_table_name varchar(64) := '';
	strat_inv_filter text := '';
	inv_filter text := '';
	inv_fips_filter text := '';
	strategy_messages_dataset_id integer := null;
	strategy_messages_table_name varchar(64) := '';
	county_dataset_id integer := null;
	county_dataset_version integer := null;
	region RECORD;
	control_program RECORD;
	inventory_record RECORD;
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
	cnt integer := 0;
BEGIN

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
		case when length(trim(cs.filter)) > 0 then '(' || cs.filter || ')' else null end,
		cs.cost_year,
		cs.analysis_year,
		cs.county_dataset_id,
		cs.county_dataset_version,
		cs.use_cost_equations,
		cs.discount_rate / 100
	FROM emf.control_strategies cs
	where cs.id = int_control_strategy_id
	INTO target_pollutant_id,
		strat_inv_filter,
		cost_year,
		inventory_year,
		county_dataset_id,
		county_dataset_version,
		use_cost_equations,
		discount_rate;

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


	-- setup temp table -- it will help track issues...
	EXECUTE '
		CREATE TEMP TABLE packet_issues (
			packet_type character varying(255),
			packet_name character varying(255),
			packet_dataset_id integer,
			packet_dataset_version integer,
			packet_dataset_name character varying(255),
			inventory_dataset_id integer,
			inventory_dataset_version integer,
			inventory_dataset_name character varying(255),
			applies_to_inventory boolean
		) ON COMMIT DROP;';


	-- cursor over inventories
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

		where inv.control_strategy_id = ' || int_control_strategy_id
	LOOP

		-- get the input dataset info
		select inventory_record.table_name
		into inv_table_name;

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

		-- get month of the dataset, 0 (Zero) indicates an annual inventory
		select public.get_dataset_month(inventory_record.dataset_id)
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

		-- build version info into where clause filter
		inv_filter := '(' || public.build_version_where_filter(inventory_record.dataset_id, inventory_record.dataset_version, 'inv') || ')' || coalesce(' and ' || public.alias_filter(strat_inv_filter, inv_table_name, 'inv'), '');


raise notice '%', inv_filter;
raise notice '%', 'give warning if CE is 100 and was assumed to be 0';
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
			message_type,
			message,
			inventory
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
			''Inventory Level''::character varying(255) as message_type,
			''Source has a 100 ceff but has an emission.'' as "message",
			null::character varying(255) as inventory
		FROM emissions.' || inv_table_name || ' inv
		where 	inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) <> 0.0'
		|| ' and ' || inv_filter;


		-- now lets evaluate each packet and see what we find
		-- Things to look for:
		--	- Program doesn't apply to any inventory, then report this
		--	- Program does apply to at least one inventory, then report other inventories that aren't affected
		--	- If Program does apply to inventory in some fashion, then report packets records that don't affect 
		--	  any sources in that inventory
raise notice '%', 'now lets evaluate each packet and see what we find';
		FOR control_program IN EXECUTE 
			'select cp."name" as control_program_name, cpt."name" as type, lower(i.table_name) as table_name, 
				cp.start_date, cp.end_date, 
				cp.dataset_id, cp.dataset_version,
				dt."name" as dataset_type, d."name" as dataset_name
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

				--raise notice '%', 
				execute 
				'select 
					count(1) as cnt
				FROM emissions.' || inv_table_name || ' inv

					inner join emissions.' || control_program.table_name || ' pc
					on pc.fips = inv.fips
					' || case when is_point_table then '
					and pc.plantid = inv.plantid
					and coalesce(pc.pointid, inv.pointid) = inv.pointid
					and coalesce(pc.stackid, inv.stackid) = inv.stackid
					and coalesce(pc.segment, inv.segment) = inv.segment
					' else '' end || '

					-- only keep if before cutoff date
					and coalesce(pc.effective_date, ''1/1/1900'')::timestamp without time zone < ''' || effective_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone

					and ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'pc') || '

				where 	' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					' || case when not is_point_table then '
					and pc.plantid is null
					and pc.pointid is null
					and pc.stackid is null
					and pc.segment is null
					' else '
					' end || '
				limit 1' -- only need to return one...
				into cnt;

				-- if there are packet records that are applied, then go ahead and report the packet records that didn't affect any sources...
				if cnt > 0 then
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
						pc.fips,
						null::character varying(10) as scc,
						pc.plantid, 
						pc.pointid, 
						pc.stackid, 
						pc.segment, 
						null::character varying(16) as poll,
						''Warning''::character varying(11) as status,
						' || quote_literal(control_program.control_program_name) || '::character varying(255) as control_program,
						''Inventory Level''::character varying(255) as message_type,
						''Plant'' || case when pc.plant is not null and length(pc.plant) > 0 then coalesce('', '' || pc.plant || '','', '''') else '''' end || '' is missing from the inventory.'' as "comment",
						' || quote_literal(inventory_record.dataset_name) || '::character varying(255) as inventory

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
						and inv.record_id is null

					where 	
						-- only keep if before cutoff date
						pc.effective_date::timestamp without time zone < ''' || effective_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone
						and ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'pc') || '';
						
				end if;

			-- see if there any matching issues with the projection, control, or allowable packet
			ELSEIF control_program.type = 'Projection' or control_program.type = 'Control' or control_program.type = 'Allowable' THEN

				-- make sure the dataset type is right...
				IF (control_program.type = 'Projection' and control_program.dataset_type = 'Projection Packet') 
					or (control_program.type = 'Control' and control_program.dataset_type = 'Control Packet')  
					or (control_program.type = 'Allowable' and control_program.dataset_type = 'Allowable Packet') THEN
raise notice '%', 'first see if packet affects anything -- packet type, ' || control_program.type || ', packet, ' || control_program.dataset_id || ', inv ' || inventory_record.dataset_id;


					-- first see if there is any issues
					execute '
					select count(1) 
					 
					from (
						' ||

						public.build_project_future_year_inventory_matching_hierarchy_sql(
							inventory_record.dataset_id, --inv_dataset_id integer, 
							inventory_record.dataset_version, --inv_dataset_version integer, 
							control_program.dataset_id, --control_program_dataset_id integer, 
							control_program.dataset_version, --control_program_dataset_version integer, 
							'fips,plantid,pointid,stackid,segment,scc,poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message', --select_columns varchar, 
							strat_inv_filter,--null'substring(fips,1,2) = ''37''', --inv_filter text,
							county_dataset_id, --1279 county_dataset_id integer,
							county_dataset_version, --county_dataset_version integer,
							case 
								when control_program.type = 'Control' then 
									'coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone and application_control = ''Y''' 
								when control_program.type = 'Allowable' then 
									'coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone' 
								else 
									null::text 
							end,
							1 --match_type integer	-- 1 = include only Matched Sources, 2 = Include packet records that didn't affect a source, 3 = ?
							)

					|| 'limit 1 -- only need to return one...
					) tbl'
					into cnt;

raise notice '%', 'first see if packet affects anything -- packet type, ' || control_program.type || ', packet, ' || control_program.dataset_id || ', inv ' || inventory_record.dataset_id || ', cnt ' || cnt;
					if cnt > 0 then
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
							''Warning''::character varying(11) as status,
							' || quote_literal(control_program.control_program_name) || ' as control_program_name,
							''Inventory Level''::character varying(255) as message_type,
							message,
							' || quote_literal(inventory_record.dataset_name) || '::character varying(255) as inventory

						from (
							--placeholder helps dealing with point vs non-point inventories, i dont have to worry about the union all statements
							select 
								null::integer as record_id,null::character varying as fips,null::character varying as plantid,null::character varying as pointid,null::character varying as stackid,null::character varying as segment,null::character varying as scc,null::character varying as poll,null::character varying as message,null::double precision as ranking
							where 1 = 0

							union all ' ||

							public.build_project_future_year_inventory_matching_hierarchy_sql(
								inventory_record.dataset_id, --inv_dataset_id integer, 
								inventory_record.dataset_version, --inv_dataset_version integer, 
								control_program.dataset_id, --control_program_dataset_id integer, 
								control_program.dataset_version, --control_program_dataset_version integer, 
								'fips,plantid,pointid,stackid,segment,scc,poll,''' || control_program.type || ' packet record does not affect any inventory records.'' as message', --select_columns varchar, 
								strat_inv_filter,--null'substring(fips,1,2) = ''37''', --inv_filter text,
								county_dataset_id, --1279 county_dataset_id integer,
								county_dataset_version, --county_dataset_version integer,
								case 
									when control_program.type = 'Control' then 
										'coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone and application_control = ''Y''' 
									when control_program.type = 'Allowable' then 
										'coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone' 
									else 
										null::text 
								end,
								2 --match_type integer	-- 1 = include only Matched Sources, 2 = Include packet records that didn't affect a source, 3 = ?
								)

						|| ') tbl';
					END IF;

				END IF;
			END IF;

			execute
			--raise notice '%',
			 'insert into packet_issues
				(
				packet_type, 
				packet_name, 
				packet_dataset_id, 
				packet_dataset_version, 
				packet_dataset_name, 
				inventory_dataset_id, 
				inventory_dataset_version, 
				inventory_dataset_name, 
				applies_to_inventory
				)
			select 
				' || quote_literal(control_program.type) || ',
				' || quote_literal(control_program.control_program_name) || ',
				' || quote_literal(control_program.dataset_id) || ',
				' || quote_literal(control_program.dataset_version) || ',
				' || quote_literal(control_program.dataset_name) || ',
				' || quote_literal(inventory_record.dataset_id) || ',
				' || quote_literal(inventory_record.dataset_version) || ',
				' || quote_literal(inventory_record.dataset_name) || ',
				' || case when cnt > 0 then true else false end || '::boolean;';


		END LOOP;
	END LOOP;



	-- packet affected some inventories, but not all, let client know which ones weren't affected
	execute 'insert into emissions.' || strategy_messages_table_name || ' 
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
		''Warning''::character varying(11) as status,
		packet_name::character varying(255) as control_program,
		''Packet Level''::character varying(255) as message_type,
		''Control Program doesn''''t apply to any sources in the inventory, '' || inventory_dataset_name || ''.''::character varying(255) as message,
		inventory_dataset_name as inventory
		from packet_issues
		where packet_name in (
			select packet_name 
			from packet_issues
			group by packet_name
			having sum(case when applies_to_inventory then 1 else 0 end) <> sum(1)
				and sum(case when applies_to_inventory then 1 else 0 end) > 0
		)
		and applies_to_inventory = false';

	-- packet doesn't affect any inventories
	execute 'insert into emissions.' || strategy_messages_table_name || ' 
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
		''Warning''::character varying(11) as status,
		packet_name::character varying(255) as control_program,
		''Packet Level''::character varying(255) as message_type,
		''Control Program doesn''''t apply to any inventories.''::character varying(255) as message,
		null::character varying(255) as inventory
		from packet_issues
		group by packet_name
		having sum(case when not applies_to_inventory then 1 else 0 end) = sum(1)';


END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;