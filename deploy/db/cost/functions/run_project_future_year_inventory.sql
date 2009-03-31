CREATE OR REPLACE FUNCTION public.run_project_future_year_inventory(
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
	control_program_technologies_count integer := 0;
	control_program_measures_count integer := 0;
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
	has_mact_column boolean := false;
	has_target_pollutant integer := 0;
	has_rpen_column boolean := false;
	has_cpri_column boolean := false;
	has_primary_device_type_code_column boolean := false;
	has_latlong_columns boolean := false;
	has_plant_column boolean := false;
	annualized_uncontrolled_emis_sql character varying;
	uncontrolled_emis_sql character varying;
	emis_sql character varying;
	percent_reduction_sql character varying;
	insert_column_list_sql character varying := '';
	select_column_list_sql character varying := '';
	column_name character varying;
	get_strategt_cost_sql character varying;
	get_strategt_cost_inner_sql character varying;
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

	-- see if there is lat & long columns in the inventory
	has_latlong_columns := public.check_table_for_columns(inv_table_name, 'xloc,yloc', ',');

	-- see if there is lat & long columns in the inventory
	has_plant_column := public.check_table_for_columns(inv_table_name, 'plant', ',');


	-- get strategy constraints
	SELECT max_emis_reduction,
		max_control_efficiency,
		min_cost_per_ton,
		min_ann_cost,
		replacement_control_min_eff_diff
	FROM emf.control_strategy_constraints csc
	where csc.control_strategy_id = control_strategy_id
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

	uncontrolled_emis_sql := 
			case 
				when dataset_month != 0 then 
					'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
				else 
					'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then inv.ann_emis / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
			end;
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






	-- see if their was a county dataset specified for the strategy, is so then build a sql where clause filter for later use
	IF county_dataset_id is not null THEN
		county_dataset_filter_sql := ' and inv.fips in (SELECT fips
			FROM emissions.' || (SELECT table_name FROM emf.internal_sources where dataset_id = county_dataset_id) || '
			where ' || public.build_version_where_filter(county_dataset_id, county_dataset_version) || ')';
	END IF;
	-- build version info into where clause filter
	inv_filter := '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'inv') || ')' || coalesce(' and ' || inv_filter, '');

	raise notice '%', 'first lets do plant closures ' || clock_timestamp();


	-- need to process based on processing_order of the program, i.e., first do plant closures, next do growth, then apply controls
	-- to various sources

	-- first lets do plant closures.
  	FOR control_program IN EXECUTE 
		'select cp."name" as control_program_name, cpt."name" as type, lower(i.table_name) as table_name, 
			cp.start_date, cp.end_date, 
			cp.dataset_id, cp.dataset_version
		 from emf.control_strategy_programs csp

			inner join emf.control_programs cp
			on cp.id = csp.control_program_id

			inner join emf.control_program_types cpt
			on cpt.id = cp.control_program_type_id

			inner join emf.internal_sources i
			on i.dataset_id = cp.dataset_id
		where csp.control_strategy_id = ' || control_strategy_id || '
			and cpt."name" = ''Plant Closure''
			and ''12/31/' || inventory_year || '''::timestamp without time zone between cp.start_date and coalesce(cp.end_date, ''12/31/' || inventory_year || '''::timestamp without time zone)
		order by processing_order'
	LOOP
		-- apply plant closure control program
		IF control_program.type = 'Plant Closure' THEN

			execute
			--raise notice '%',
			 'insert into emissions.' || detailed_result_table_name || ' 
				(
				dataset_id,
				cm_abbrev,
				poll,
				scc,
				fips,
				' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '
				annual_oper_maint_cost,
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
--				input_emis,
--				output_emis,
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
				control_program,
				xloc,
				yloc,
				plant,
				"comment"
				)
			select 
				' || detailed_result_dataset_id || '::integer,
				''PLTCLOSURE'' as abbreviation,
				inv.poll,
				inv.scc,
				inv.fips,
				' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
				null::double precision as operation_maintenance_cost,
				null::double precision as annualized_capital_cost,
				null::double precision as capital_cost,
				null::double precision as ann_cost,
				null::double precision as computed_cost_per_ton,
				null::double precision as control_eff,
				null::double precision as rule_pen,
				null::double precision as rule_eff,
				null::double precision as percent_reduction,
				inv.ceff,
				' || case when is_point_table = false then 'inv.rpen' else '100' end || ',
				inv.reff,
				0.0 as final_emissions,
				' || emis_sql || ' as emis_reduction,
				' || emis_sql || ' as inv_emissions,
--				' || emis_sql || ' as input_emis,
--				0.0 as output_emis,
				1,
				substr(inv.fips, 1, 2),
				substr(inv.fips, 3, 3),
				' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ',
				' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ',
				inv.record_id::integer as source_id,
				' || input_dataset_id || '::integer,
				' || control_strategy_id || '::integer,
				null::integer as control_measures_id,
				null::varchar(255) as equation_type,
				' || quote_literal(control_program.control_program_name) || ',
				' || case when has_latlong_columns then 'inv.xloc,inv.yloc,' else 'fipscode.centerlon as xloc,fipscode.centerlat as yloc,' end || '
				' || case when has_plant_column then 'inv.plant' when not has_latlong_columns then 'fipscode.state_county_fips_code_desc as plant' else 'null::character varying as plant' end || ',
				''''
			FROM emissions.' || inv_table_name || ' inv

				inner join emissions.' || control_program.table_name || ' pc
				on pc.fips = inv.fips
				' || case when is_point_table then '
				and pc.plantid = inv.plantid
				and coalesce(pc.pointid, inv.pointid) = inv.pointid
				and coalesce(pc.stackid, inv.stackid) = inv.stackid
				and coalesce(pc.segment, inv.segment) = inv.segment
				' else '' end || '

				-- make sure and keep even if in the same year, they might not have closed, and dont worry about prorating...
				and date_part(''year'', pc.effective_date::timestamp without time zone) < ' || inventory_year || '

				and ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'pc') || '

				' || case when not has_latlong_columns then 'left outer join reference.fips fipscode
				on fipscode.state_county_fips = inv.fips
				and fipscode.country_num = ''0''' else '' end || '

			where 	' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
				' || case when not is_point_table then '
				and pc.plantid is null
				and pc.pointid is null
				and pc.stackid is null
				and pc.segment is null
				' else '
				' end || '';

		END IF;

	END LOOP;

	raise notice '%', 'next lets process projections ' || clock_timestamp();

	-- next lets process projections, need to union the the various program tables together, so we can make sure and get the most source specific projection
  	FOR control_program IN EXECUTE 
		'select cp."name" as control_program_name, cpt."name" as type, lower(i.table_name) as table_name, 
			cp.start_date, cp.end_date, 
			cp.dataset_id, cp.dataset_version, dt."name" as dataset_type
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
			and cpt."name" = ''Projection''
			and ''12/31/' || inventory_year || '''::timestamp without time zone between cp.start_date and coalesce(cp.end_date, ''12/31/' || inventory_year || '''::timestamp without time zone)
		order by processing_order'
	LOOP
		-- make sure the dataset type is right...
		IF control_program.dataset_type = 'Projection Packet' THEN
			--store control dataset version filter in variable
			select public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'proj')
			into control_program_dataset_filter_sql;
			
			If length(sql) > 0 THEN 
				sql := sql || ' union all ';
			END IF;
			--see http://www.smoke-model.org/version2.4/html/ch06s02.html for source matching hierarchy
			sql := sql || '
			select distinct on (record_id)
				record_id,proj_factor,
				' || quote_literal(control_program.control_program_name) || ' as control_program_name, ranking
			from (
				--placeholder helps dealing with point vs non-point inventories, i dont have to worry about the union all statements
				select 
					null::integer as record_id, null::double precision as proj_factor, null::integer as ranking
				where 1 = 0

				' || case when is_point_table then '
				--1
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.stackid is not null) and (proj.segment is not null) and (proj.scc is not null) and (proj.poll is not null) then 1::smallint --1
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					and proj.plantid = inv.plantid
					and proj.pointid = inv.pointid
					and proj.stackid = inv.stackid
					and proj.segment = inv.segment
					and proj.scc = inv.scc
					and proj.poll = inv.poll

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--2
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.stackid is not null) and (proj.segment is not null) and (proj.poll is not null) then 2::smallint --2
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.pointid = inv.pointid
					and proj.stackid = inv.stackid
					and proj.segment = inv.segment
					and proj.poll = inv.poll

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--3
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.stackid is not null) and (proj.poll is not null) then 3::smallint --3
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.pointid = inv.pointid
					and proj.stackid = inv.stackid
					and proj.poll = inv.poll

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--4
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.poll is not null) then 4::smallint --4
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.pointid = inv.pointid
					and proj.poll = inv.poll

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--5
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.scc is not null) and (proj.poll is not null) then 5::smallint --5
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.scc = inv.scc
					and proj.poll = inv.poll

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--6
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.poll is not null) then 6::smallint --6
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.poll = inv.poll

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--7
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.stackid is not null) and (proj.segment is not null) and (proj.scc is not null) then 7::smallint --7
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.pointid = inv.pointid
					and proj.stackid = inv.stackid
					and proj.segment = inv.segment
					and proj.scc = inv.scc

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--8
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.stackid is not null) and (proj.segment is not null) then 8::smallint --8
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.pointid = inv.pointid
					and proj.stackid = inv.stackid
					and proj.segment = inv.segment

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--9
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.stackid is not null) then 9::smallint --9
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.pointid = inv.pointid
					and proj.stackid = inv.stackid

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--10
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) then 10::smallint --10
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.pointid = inv.pointid

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--11
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.scc is not null) then 11::smallint --11
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.scc = inv.scc

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--12
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) then 12::smallint --12
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
				' else '' end || '
				
				' || case when has_mact_column then '
				--13,15
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.mact is not null) and (proj.scc is not null) and (proj.poll is not null) then 13::smallint --13
						when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.mact is not null) and (proj.scc is not null) and (proj.poll is not null) then 15::smallint --15
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
					
					and proj.mact = inv.mact
					and proj.scc = inv.scc
					and proj.poll = inv.poll

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--14,16
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.mact is not null) and (proj.poll is not null) then 14::smallint --14
						when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.mact is not null) and (proj.poll is not null) then 16::smallint --16
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
					
					and proj.mact = inv.mact
					and proj.poll = inv.poll

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--17
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.mact is not null) and (proj.scc is not null) and (proj.poll is not null) then 17::smallint --17
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.mact = inv.mact
					and proj.scc = inv.scc
					and proj.poll = inv.poll

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--18
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.mact is not null) and (proj.poll is not null) then 18::smallint --18
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.mact = inv.mact
					and proj.poll = inv.poll

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--19,21
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.mact is not null) and (proj.scc is not null) then 19::smallint --19
						when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.mact is not null) and (proj.scc is not null) then 21::smallint --21
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
					
					and proj.mact = inv.mact
					and proj.scc = inv.scc

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--20,22
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.mact is not null) then 20::smallint --20
						when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.mact is not null) then 22::smallint --22
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
					
					and proj.mact = inv.mact

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--23
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.mact is not null) and (proj.scc is not null) then 23::smallint --23
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.mact = inv.mact
					and proj.scc = inv.scc

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--24
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.mact is not null) then 24::smallint --24
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.mact = inv.mact

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
				' else '' end || '

				' || case when has_sic_column then '
				--25,27
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.sic is not null) and (proj.poll is not null) then 25::smallint --25
						when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.sic is not null) and (proj.poll is not null) then 27::smallint --27
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
					and proj.sic = inv.sic
					and proj.poll = inv.poll

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--29
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.sic is not null) and (proj.poll is not null) then 29::smallint --29
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.sic = inv.sic
					and proj.poll = inv.poll

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--31,33
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.sic is not null) then 31::smallint --31
						when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.sic is not null) then 33::smallint --33
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
					and proj.sic = inv.sic

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--35
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.sic is not null) then 35::smallint --35
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.sic = inv.sic

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
				' else '' end || '

				--37,41
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.scc is not null) and (proj.poll is not null) then 37::smallint --37
						when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.scc is not null) and (proj.poll is not null) then 41::smallint --41
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
					and proj.scc = inv.scc
					and proj.poll = inv.poll

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--45
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.scc is not null) and (proj.poll is not null) then 45::smallint --45
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.scc = inv.scc
					and proj.poll = inv.poll

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--49,53
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.scc is not null) then 49::smallint --49
						when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.scc is not null) then 53::smallint --53
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
					and proj.scc = inv.scc

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--57
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.scc is not null) then 57::smallint --57
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.scc = inv.scc

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--61,63
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.poll is not null) then 61::smallint --61
						when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.poll is not null) then 63::smallint --63
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
					and proj.poll = inv.poll

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--62,64
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) then 62::smallint --62
						when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) then 64::smallint --64
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '

				--65
				union all
				select 
					inv.record_id,proj.proj_factor,
					case 
						when (proj.poll is not null) then 65::smallint --65
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.poll = inv.poll

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
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
			) tbl';

/*
			sql := sql || '
				select fips, 
					scc, 
					poll, 
					PROJ_FACTOR, 
					sic, 
					mact, 
					plantid, 
					pointid, 
					stackid, 
					segment,
					' || quote_literal(control_program.control_program_name) || ' as control_program_name
				from emissions.' || control_program.table_name || ' p
				where ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'p') || '';
*/
		END IF;

	END LOOP;
--		raise notice '%', sql;

	IF length(sql) > 0 THEN
		sql := sql || ' order by record_id, ranking';
		-- make sure the apply order is 1, this should be the first thing happening to a source....this is important when the controlled inventpory is created.
		execute 
		--raise notice '%', 
			'insert into emissions.' || detailed_result_table_name || ' 
			(
			dataset_id,
			cm_abbrev,
			poll,
			scc,
			fips,
			' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '
			annual_oper_maint_cost,
			annualized_capital_cost,
			total_capital_cost,
			annual_cost,
			ann_cost_per_ton,
			control_eff,
			rule_pen,
			rule_eff,
			percent_reduction,
			adj_factor,
			inv_ctrl_eff,
			inv_rule_pen,
			inv_rule_eff,
			final_emissions,
			emis_reduction,
			inv_emissions,
	--				input_emis,
	--				output_emis,
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
			control_program,
			xloc,
			yloc,
			plant,
			"comment"
			)
		select 
			' || detailed_result_dataset_id || '::integer,
			''PROJECTION'' as abbreviation,
			inv.poll,
			inv.scc,
			inv.fips,
			' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
			null::double precision as operation_maintenance_cost,
			null::double precision as annualized_capital_cost,
			null::double precision as capital_cost,
			null::double precision as ann_cost,
			null::double precision as computed_cost_per_ton,
			null::double precision as control_eff,
			null::double precision as rule_pen,
			null::double precision as rule_eff,
			null::double precision as percent_reduction,
			proj.PROJ_FACTOR as adj_factor,
			inv.ceff,
			' || case when is_point_table = false then 'inv.rpen' else '100' end || ',
			inv.reff,
			' || emis_sql || ' * proj.proj_factor as final_emissions,
			' || emis_sql || ' - ' || emis_sql || ' * proj.proj_factor as emis_reduction,
			' || emis_sql || ' as inv_emissions,
	--				' || emis_sql || ' as input_emis,
	--				0.0 as output_emis,
			1,
			substr(inv.fips, 1, 2),
			substr(inv.fips, 3, 3),
			' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ',
			' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ',
			inv.record_id::integer as source_id,
			' || input_dataset_id || '::integer,
			' || control_strategy_id || '::integer,
			null::integer as control_measures_id,
			null::varchar(255) as equation_type,
			proj.control_program_name,
			' || case when has_latlong_columns then 'inv.xloc,inv.yloc,' else 'fipscode.centerlon as xloc,fipscode.centerlat as yloc,' end || '
			' || case when has_plant_column then 'inv.plant' when not has_latlong_columns then 'fipscode.state_county_fips_code_desc as plant' else 'null::character varying as plant' end || ',
			''''
		FROM emissions.' || inv_table_name || ' inv

			inner join (' || sql || ') proj
			on proj.record_id = inv.record_id

			' || case when not has_latlong_columns then 'left outer join reference.fips fipscode
			on fipscode.state_county_fips = inv.fips
			and fipscode.country_num = ''0''' else '' end || '

		where 	' || inv_filter || coalesce(county_dataset_filter_sql, '') || '';
	END IF;


	-- next lets process controls, need to union the the various program tables together, so we can make sure and get the most source specific controls
	sql := ''; --reset
	-- see if control strategy program have specific measures or technologies specified
	select count(cptech.id),
		count(cpm.id)
	 from emf.control_strategy_programs csp
		inner join emf.control_programs cp
		on cp.id = csp.control_program_id

		inner join emf.control_program_types cpt
		on cpt.id = cp.control_program_type_id

		left outer join emf.control_program_technologies cptech
		on cptech.control_program_id = cp.id
		
		left outer join emf.control_program_measures cpm
		on cpm.control_program_id = cp.id
		
	where csp.control_strategy_id = control_strategy_id
		and cpt."name" = 'Control'
	into control_program_technologies_count, 
		control_program_measures_count;
  	FOR control_program IN EXECUTE 
		'select cp.id as control_program_id, cp."name" as control_program_name, cpt."name" as type, lower(i.table_name) as table_name, 
			cp.start_date, cp.end_date, 
			cp.dataset_id, cp.dataset_version, dt."name" as dataset_type,
			(select count(*) from emf.control_program_technologies cptech where cptech.control_program_id = cp.id) as control_program_technologies_count,
			(select count(*) from emf.control_program_measures cpm where cpm.control_program_id = cp.id) as control_program_measures_count
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
			and cpt."name" = ''Control''
			and ''12/31/' || inventory_year || '''::timestamp without time zone between cp.start_date and coalesce(cp.end_date, ''12/31/' || inventory_year || '''::timestamp without time zone)
		order by processing_order'
	LOOP
		-- make sure the dataset type is right...
		IF control_program.dataset_type = 'Control Packet' THEN
			--store control dataset version filter in variable
			select public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'cont')
			into control_program_dataset_filter_sql;
			
			If length(sql) > 0 THEN 
				sql := sql || ' union all ';
			END IF;
			--see http://www.smoke-model.org/version2.4/html/ch06s02.html for source matching hierarchy
			sql := sql || '
			select distinct on (record_id)
				record_id,ceff,rpen,reff,pri_cm_abbrev,
				' || quote_literal(control_program.control_program_name) || ' as control_program_name, ' || control_program.control_program_id || ' as control_program_id,
				' || control_program.control_program_technologies_count || ' as control_program_technologies_count, ' || control_program.control_program_measures_count || ' as control_program_measures_count, 
				ranking
			from (
				--placeholder helps dealing with point vs non-point inventories, i dont have to worry about the union all statements
				select 
					null::integer as record_id, null::double precision as ceff, null::double precision as rpen,
					null::double precision as reff, null::character varying(10) as pri_cm_abbrev, null::integer as ranking
				where 1 = 0

				' || case when is_point_table then '
				--1
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.plantid is not null) and (cont.pointid is not null) and (cont.stackid is not null) and (cont.segment is not null) and (cont.scc is not null) and (cont.poll is not null) then 1::smallint --1
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.fips = inv.fips
					and cont.plantid = inv.plantid
					and cont.pointid = inv.pointid
					and cont.stackid = inv.stackid
					and cont.segment = inv.segment
					and cont.scc = inv.scc
					and cont.poll = inv.poll

				where cont.fips is not null 
					and cont.plantid is not null 
					and cont.pointid is not null 
					and cont.stackid is not null 
					and cont.segment is not null 
					and cont.scc is not null 
					and cont.poll is not null
					and cont.sic is null
					and cont.mact is null

					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--2
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.plantid is not null) and (cont.pointid is not null) and (cont.stackid is not null) and (cont.segment is not null) and (cont.poll is not null) then 2::smallint --2
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.fips = inv.fips
					
					and cont.plantid = inv.plantid
					and cont.pointid = inv.pointid
					and cont.stackid = inv.stackid
					and cont.segment = inv.segment
					and cont.poll = inv.poll

				where cont.fips is not null 
					and cont.plantid is not null 
					and cont.pointid is not null 
					and cont.stackid is not null 
					and cont.segment is not null 
					and cont.scc is null
					and cont.poll is not null
					and cont.sic is null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--3
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.plantid is not null) and (cont.pointid is not null) and (cont.stackid is not null) and (cont.poll is not null) then 3::smallint --3
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.fips = inv.fips
					
					and cont.plantid = inv.plantid
					and cont.pointid = inv.pointid
					and cont.stackid = inv.stackid
					and cont.poll = inv.poll

				where cont.fips is not null 
					and cont.plantid is not null 
					and cont.pointid is not null 
					and cont.stackid is not null 
					and cont.segment is null 
					and cont.scc is null
					and cont.poll is not null
					and cont.sic is null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--4
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.plantid is not null) and (cont.pointid is not null) and (cont.poll is not null) then 4::smallint --4
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.fips = inv.fips
					
					and cont.plantid = inv.plantid
					and cont.pointid = inv.pointid
					and cont.poll = inv.poll

				where cont.fips is not null 
					and cont.plantid is not null 
					and cont.pointid is not null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is null
					and cont.poll is not null
					and cont.sic is null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--5
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.plantid is not null) and (cont.scc is not null) and (cont.poll is not null) then 5::smallint --5
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.fips = inv.fips
					
					and cont.plantid = inv.plantid
					and cont.scc = inv.scc
					and cont.poll = inv.poll

				where cont.fips is not null 
					and cont.plantid is not null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is not null 
					and cont.poll is not null
					and cont.sic is null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--6
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.plantid is not null) and (cont.poll is not null) then 6::smallint --6
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.fips = inv.fips
					
					and cont.plantid = inv.plantid
					and cont.poll = inv.poll

				where cont.fips is not null 
					and cont.plantid is not null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is null
					and cont.poll is not null
					and cont.sic is null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--7
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.plantid is not null) and (cont.pointid is not null) and (cont.stackid is not null) and (cont.segment is not null) and (cont.scc is not null) then 7::smallint --7
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.fips = inv.fips
					
					and cont.plantid = inv.plantid
					and cont.pointid = inv.pointid
					and cont.stackid = inv.stackid
					and cont.segment = inv.segment
					and cont.scc = inv.scc

				where cont.fips is not null 
					and cont.plantid is not null 
					and cont.pointid is not null 
					and cont.stackid is not null 
					and cont.segment is not null 
					and cont.scc is not null 
					and cont.poll is null
					and cont.sic is null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--8
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.plantid is not null) and (cont.pointid is not null) and (cont.stackid is not null) and (cont.segment is not null) then 8::smallint --8
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.fips = inv.fips
					
					and cont.plantid = inv.plantid
					and cont.pointid = inv.pointid
					and cont.stackid = inv.stackid
					and cont.segment = inv.segment

				where cont.fips is not null 
					and cont.plantid is not null 
					and cont.pointid is not null 
					and cont.stackid is not null 
					and cont.segment is not null 
					and cont.scc is null
					and cont.poll is null
					and cont.sic is null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--9
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.plantid is not null) and (cont.pointid is not null) and (cont.stackid is not null) then 9::smallint --9
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.fips = inv.fips
					
					and cont.plantid = inv.plantid
					and cont.pointid = inv.pointid
					and cont.stackid = inv.stackid

				where cont.fips is not null 
					and cont.plantid is not null 
					and cont.pointid is not null 
					and cont.stackid is not null 
					and cont.segment is null 
					and cont.scc is null
					and cont.poll is null
					and cont.sic is null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--10
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.plantid is not null) and (cont.pointid is not null) then 10::smallint --10
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.fips = inv.fips
					
					and cont.plantid = inv.plantid
					and cont.pointid = inv.pointid

				where cont.fips is not null 
					and cont.plantid is not null 
					and cont.pointid is not null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is null
					and cont.poll is null
					and cont.sic is null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--11
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.plantid is not null) and (cont.scc is not null) then 11::smallint --11
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.fips = inv.fips
					
					and cont.plantid = inv.plantid
					and cont.scc = inv.scc

				where cont.fips is not null 
					and cont.plantid is not null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is not null 
					and cont.poll is null
					and cont.sic is null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--12
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.plantid is not null) then 12::smallint --12
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.fips = inv.fips
					
					and cont.plantid = inv.plantid

				where cont.fips is not null 
					and cont.plantid is not null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is null
					and cont.poll is null
					and cont.sic is null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone
				' else '' end || '

				' || case when has_mact_column then '
				--13,15
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.mact is not null) and (cont.scc is not null) and (cont.poll is not null) then 13::smallint --13
						when (cont.fips is not null) and (length(cont.fips) = 2 or length(cont.fips) = 3) and (cont.mact is not null) and (cont.scc is not null) and (cont.poll is not null) then 15::smallint --15
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on (cont.fips = inv.fips or cont.fips = substr(inv.fips, 1, 2))
					
					and cont.mact = inv.mact
					and cont.scc = inv.scc
					and cont.poll = inv.poll

				where cont.fips is not null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is not null 
					and cont.poll is not null
					and cont.sic is null
					and cont.mact is not null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--14,16
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.mact is not null) and (cont.poll is not null) then 14::smallint --14
						when (cont.fips is not null) and (length(cont.fips) = 2 or length(cont.fips) = 3) and (cont.mact is not null) and (cont.poll is not null) then 16::smallint --16
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on (cont.fips = inv.fips or cont.fips = substr(inv.fips, 1, 2))
					
					and cont.mact = inv.mact
					and cont.poll = inv.poll

				where cont.fips is not null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is null
					and cont.poll is not null
					and cont.sic is null
					and cont.mact is not null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--17
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.mact is not null) and (cont.scc is not null) and (cont.poll is not null) then 17::smallint --17
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.mact = inv.mact
					and cont.scc = inv.scc
					and cont.poll = inv.poll

				where cont.fips is null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is not null 
					and cont.poll is not null
					and cont.sic is null
					and cont.mact is not null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--18
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.mact is not null) and (cont.poll is not null) then 18::smallint --18
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.mact = inv.mact
					and cont.poll = inv.poll

				where cont.fips is null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is null
					and cont.poll is not null
					and cont.sic is null
					and cont.mact is not null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--19,21
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.mact is not null) and (cont.scc is not null) then 19::smallint --19
						when (cont.fips is not null) and (length(cont.fips) = 2 or length(cont.fips) = 3) and (cont.mact is not null) and (cont.scc is not null) then 21::smallint --21
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on (cont.fips = inv.fips or cont.fips = substr(inv.fips, 1, 2))
					
					and cont.mact = inv.mact
					and cont.scc = inv.scc

				where cont.fips is not null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is not null 
					and cont.poll is null
					and cont.sic is null
					and cont.mact is not null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--20,22
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.mact is not null) then 20::smallint --20
						when (cont.fips is not null) and (length(cont.fips) = 2 or length(cont.fips) = 3) and (cont.mact is not null) then 22::smallint --22
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on (cont.fips = inv.fips or cont.fips = substr(inv.fips, 1, 2))
					
					and cont.mact = inv.mact

				where cont.fips is not null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is null
					and cont.poll is null
					and cont.sic is null
					and cont.mact is not null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--23
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.mact is not null) and (cont.scc is not null) then 23::smallint --23
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.mact = inv.mact
					and cont.scc = inv.scc

				where cont.fips is null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is not null 
					and cont.poll is null
					and cont.sic is null
					and cont.mact is not null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--24
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.mact is not null) then 24::smallint --24
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.mact = inv.mact

				where cont.fips is null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is null
					and cont.poll is null
					and cont.sic is null
					and cont.mact is not null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone
				' else '' end || '

				' || case when has_sic_column then '
				--25,27
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.sic is not null) and (cont.poll is not null) then 25::smallint --25
						when (cont.fips is not null) and (length(cont.fips) = 2 or length(cont.fips) = 3) and (cont.sic is not null) and (cont.poll is not null) then 27::smallint --27
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on (cont.fips = inv.fips or cont.fips = substr(inv.fips, 1, 2))
					and cont.sic = inv.sic
					and cont.poll = inv.poll

				where cont.fips is not null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is null
					and cont.poll is not null
					and cont.sic is not null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--29
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.sic is not null) and (cont.poll is not null) then 29::smallint --29
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.sic = inv.sic
					and cont.poll = inv.poll

				where cont.fips is null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is null
					and cont.poll is not null
					and cont.sic is not null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--31,33
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.sic is not null) then 31::smallint --31
						when (cont.fips is not null) and (length(cont.fips) = 2 or length(cont.fips) = 3) and (cont.sic is not null) then 33::smallint --33
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on (cont.fips = inv.fips or cont.fips = substr(inv.fips, 1, 2))
					and cont.sic = inv.sic

				where cont.fips is not null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is null
					and cont.poll is null
					and cont.sic is not null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--35
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.sic is not null) then 35::smallint --35
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.sic = inv.sic

				where cont.fips is null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is null
					and cont.poll is null
					and cont.sic is not null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone
				' else '' end || '

				--37,41
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.scc is not null) and (cont.poll is not null) then 37::smallint --37
						when (cont.fips is not null) and (length(cont.fips) = 2 or length(cont.fips) = 3) and (cont.scc is not null) and (cont.poll is not null) then 41::smallint --41
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on (cont.fips = inv.fips or cont.fips = substr(inv.fips, 1, 2))
					and cont.scc = inv.scc
					and cont.poll = inv.poll

				where cont.fips is not null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is not null 
					and cont.poll is not null
					and cont.sic is null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--45
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.scc is not null) and (cont.poll is not null) then 45::smallint --45
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.scc = inv.scc
					and cont.poll = inv.poll

				where cont.fips is null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is not null 
					and cont.poll is not null
					and cont.sic is null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--49,53
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.scc is not null) then 49::smallint --49
						when (cont.fips is not null) and (length(cont.fips) = 2 or length(cont.fips) = 3) and (cont.scc is not null) then 53::smallint --53
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on (cont.fips = inv.fips or cont.fips = substr(inv.fips, 1, 2))
					and cont.scc = inv.scc

				where cont.fips is not null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is not null 
					and cont.poll is null
					and cont.sic is null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--57
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.scc is not null) then 57::smallint --57
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.scc = inv.scc

				where cont.fips is null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is not null 
					and cont.poll is null
					and cont.sic is null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--61,63
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) and (cont.poll is not null) then 61::smallint --61
						when (cont.fips is not null) and (length(cont.fips) = 2 or length(cont.fips) = 3) and (cont.poll is not null) then 63::smallint --63
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on (cont.fips = inv.fips or cont.fips = substr(inv.fips, 1, 2))
					and cont.poll = inv.poll

				where cont.fips is not null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is null
					and cont.poll is not null
					and cont.sic is null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--62,64
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.fips is not null) and (length(cont.fips) = 5 or length(cont.fips) = 6) then 62::smallint --62
						when (cont.fips is not null) and (length(cont.fips) = 2 or length(cont.fips) = 3) then 64::smallint --64
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on (cont.fips = inv.fips or cont.fips = substr(inv.fips, 1, 2))

				where cont.fips is not null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is null
					and cont.poll is null
					and cont.sic is null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--65
				union all
				select 
					inv.record_id,cont.ceff,cont.rpen,cont.reff,cont.pri_cm_abbrev,
					case 
						when (cont.poll is not null) then 65::smallint --65
					end as ranking
				FROM emissions.' || control_program.table_name || ' cont
					inner join emissions.' || inv_table_name || ' inv
							
					on cont.poll = inv.poll

				where 
					cont.fips is null 
					and cont.plantid is null 
					and cont.pointid is null 
					and cont.stackid is null 
					and cont.segment is null 
					and cont.scc is null
					and cont.poll is not null
					and cont.sic is null
					and cont.mact is null
					and ' || control_program_dataset_filter_sql || '
					and application_control = ''Y'' 
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(cont.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone
			) tbl';
		END IF;

	END LOOP;
--		raise notice '%', sql;

	IF length(sql) > 0 THEN
		raise notice '%', 'next lets do controls ' || clock_timestamp();
		sql := sql || ' order by record_id, ranking';

		uncontrolled_emis_sql := 
				case 
					when dataset_month != 0 then 
						'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then case when dr.record_id is not null then dr.final_emissions else coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) end / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
					else 
						'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then case when dr.record_id is not null then dr.final_emissions else inv.ann_emis end / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
				end;
		emis_sql := 
				case 
					when dataset_month != 0 then 
						'case when dr.record_id is not null then dr.final_emissions else coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) end' 
					else 
						'case when dr.record_id is not null then dr.final_emissions else inv.ann_emis end' 
				end;
		annualized_uncontrolled_emis_sql := 
				case 
					when dataset_month != 0 then 
						'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then case when dr.record_id is not null then dr.final_emissions else coalesce(inv.avd_emis * 365, inv.ann_emis) end / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
					else 
						'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then case when dr.record_id is not null then dr.final_emissions else inv.ann_emis end / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
				end;



		percent_reduction_sql := 'cont.ceff * coalesce(cont.reff, 100) * coalesce(cont.rpen, 100) / 100 / 100';
		get_strategt_cost_sql := '(public.get_strategy_costs(' || case when use_cost_equations then 'true' else 'false' end || '::boolean, m.id, 
				abbreviation, ' || discount_rate|| ', 
				m.equipment_life, er.cap_ann_ratio, 
				er.cap_rec_factor, er.ref_yr_cost_per_ton, 
				' || uncontrolled_emis_sql || ' * ' || percent_reduction_sql || ' / 100, ' || ref_cost_year_chained_gdp || ' / cast(chained_gdp as double precision), 
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


		-- make sure the apply order is 1, this should be the first thing happening to a source....this is important when the controlled inventpory is created.
		-- the apply order will dictate how the 
		execute 
		--raise notice '%', 
		'insert into emissions.' || detailed_result_table_name || ' 
			(
			dataset_id,
			cm_abbrev,
			poll,
			scc,
			fips,
			' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '
			annual_oper_maint_cost,
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
			adj_factor,
			final_emissions,
			emis_reduction,
			inv_emissions,
	--				input_emis,
	--				output_emis,
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
			control_program,
			xloc,
			yloc,
			plant,
			"comment"
			)
		select distinct on (inv.record_id)
			' || detailed_result_dataset_id || '::integer,
			coalesce(cont.pri_cm_abbrev, case when cont.ceff <> 0 /*and abs(cont.ceff - er.efficiency) <= ' || replacement_control_min_eff_diff_constraint || '::double precision */then m.abbreviation else null::character varying end, ''UNKNOWNMSR'') as abbreviation,
			inv.poll,
			inv.scc,
			inv.fips,
			' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
			case when cont.pri_cm_abbrev is null and er.id is not null and cont.ceff <> 0 /*/*and abs(cont.ceff - er.efficiency) <= ' || replacement_control_min_eff_diff_constraint || '::double precision */*/then TO_CHAR(' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.operation_maintenance_cost, ''FM999999999999999990'')::double precision else null::double precision end as operation_maintenance_cost,
			case when cont.pri_cm_abbrev is null and er.id is not null and cont.ceff <> 0 /*and abs(cont.ceff - er.efficiency) <= ' || replacement_control_min_eff_diff_constraint || '::double precision */then TO_CHAR(' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.annualized_capital_cost, ''FM999999999999999990'')::double precision else null::double precision end as annualized_capital_cost,
			case when cont.pri_cm_abbrev is null and er.id is not null and cont.ceff <> 0 /*and abs(cont.ceff - er.efficiency) <= ' || replacement_control_min_eff_diff_constraint || '::double precision */then TO_CHAR(' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.capital_cost, ''FM999999999999999990'')::double precision else null::double precision end as capital_cost,
			case when cont.pri_cm_abbrev is null and er.id is not null and cont.ceff <> 0 /*and abs(cont.ceff - er.efficiency) <= ' || replacement_control_min_eff_diff_constraint || '::double precision */then TO_CHAR(' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.annual_cost, ''FM999999999999999990'')::double precision else null::double precision end as ann_cost,
			case when cont.pri_cm_abbrev is null and er.id is not null and cont.ceff <> 0 /*and abs(cont.ceff - er.efficiency) <= ' || replacement_control_min_eff_diff_constraint || '::double precision */then TO_CHAR(' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.computed_cost_per_ton, ''FM999999999999999990'')::double precision else null::double precision end as computed_cost_per_ton,
/*

			null::double precision as operation_maintenance_cost,
			null::double precision as annualized_capital_cost,
			null::double precision as capital_cost,
			null::double precision as ann_cost,
			null::double precision as computed_cost_per_ton,
*/			
			cont.ceff as control_eff,
			coalesce(cont.rpen, 100) as rule_pen,
			coalesce(cont.reff, 100) as rule_eff,
			cont.ceff * coalesce(cont.rpen, 100) * coalesce(cont.reff, 100) / 100 / 100 as percent_reduction,
			inv.ceff,
			' || case when is_point_table = false then 'inv.rpen' else '100' end || ',
			inv.reff,
			cont.ceff * coalesce(cont.rpen, 100) * coalesce(cont.reff, 100) / 100 / 100 / 100 as adj_factor,
			' || uncontrolled_emis_sql || ' * (1 - cont.ceff * coalesce(cont.rpen, 100) * coalesce(cont.reff, 100) / 100 / 100 / 100) as final_emissions,
			' || uncontrolled_emis_sql || ' * cont.ceff * coalesce(cont.rpen, 100) * coalesce(cont.reff, 100) / 100 / 100 / 100 as emis_reduction,
			' || uncontrolled_emis_sql || ' as inv_emissions,
	--				' || uncontrolled_emis_sql || ' as input_emis,
	--				0.0 as output_emis,
			2,
			substr(inv.fips, 1, 2),
			substr(inv.fips, 3, 3),
			' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ',
			' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ',
			inv.record_id::integer as source_id,
			' || input_dataset_id || '::integer,
			' || control_strategy_id || '::integer,
			null::integer as control_measures_id,
			case when cont.pri_cm_abbrev is null and er.id is not null and cont.ceff <> 0 and abs(cont.ceff - er.efficiency) / cont.ceff * 100 <= ' || replacement_control_min_eff_diff_constraint || '::double precision then ' || get_strategt_cost_sql || '.actual_equation_type else null::varchar(255) end as equation_type,
			cont.control_program_name,
			' || case when has_latlong_columns then 'inv.xloc,inv.yloc,' else 'fipscode.centerlon as xloc,fipscode.centerlat as yloc,' end || '
			' || case when has_plant_column then 'inv.plant' when not has_latlong_columns then 'fipscode.state_county_fips_code_desc as plant' else 'null::character varying as plant' end || ',
			''''
		FROM emissions.' || inv_table_name || ' inv

			-- see if the source was projected, if so, use the projected values
			left outer join emissions.' || detailed_result_table_name || ' dr
			on dr.source_id = inv.record_id
			and dr.input_ds_id = inv.dataset_id
			and dr.apply_order = 1

			inner join emf.pollutants p
			on p.name = inv.poll
			
			' || case when not has_latlong_columns then 'left outer join reference.fips fipscode
			on fipscode.state_county_fips = inv.fips
			and fipscode.country_num = ''0''' else '' end || '

			inner join (' || sql || ') cont
			on cont.record_id = inv.record_id

			-- tables for predicting measure
			left outer join emf.control_measure_sccs sccs
			-- scc filter
			on sccs."name" = inv.scc

			left outer join emf.control_measure_efficiencyrecords er
			on er.control_measures_id = sccs.control_measures_id
			-- poll filter
			and er.pollutant_id = p.id
			-- min and max emission filter
			and ' || annualized_uncontrolled_emis_sql || ' between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
			-- locale filter
			and (er.locale = inv.fips or er.locale = substr(inv.fips, 1, 2) or er.locale = '''')
			-- effecive date filter
			and ' || inventory_year || '::integer >= coalesce(date_part(''year'', er.effective_date), ' || inventory_year || '::integer)		


			left outer join emf.control_measures m
			on m.id = er.control_measures_id
			

			left outer join emf.control_measure_equations eq
			on eq.control_measure_id = m.id
			and eq.pollutant_id = p.id

			left outer join emf.equation_types et
			on et.id = eq.equation_type_id

			left outer join reference.gdplev
			on gdplev.annual = eq.cost_year

		where 	' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
			-- control program measure and technology filter
			' || case when control_program_measures_count > 0 and control_program_technologies_count > 0  then '
			and (
				(cont.control_program_measures_count > 0 and exists (select 1 from emf.control_program_measures cpm where cpm.control_measure_id = m.id))
				or 
				(cont.control_program_technologies_count > 0 and exists (select 1 from emf.control_program_technologies cpt where cpt.control_program_id = cont.control_program_id and cpt.control_technology_id = m.control_technology))
			)
			' when control_program_measures_count > 0 then '
			and cont.control_program_measures_count > 0 and exists (select 1 from emf.control_program_measures cpm where cpm.control_measure_id = m.id)
			' when control_program_technologies_count > 0 then '
			and cont.control_program_technologies_count > 0 and exists (select 1 from emf.control_program_technologies cpt where cpt.control_program_id = cont.control_program_id and cpt.control_technology_id = m.control_technology)
			' else '' end || '
			and (cont.ceff >= coalesce(inv.ceff, 0.0) or cont.ceff = 0.0)
		order by inv.record_id,
			case when length(er.locale) = 5 then 0 when length(er.locale) = 2 then 1 else 2 end, 	
			abs(cont.ceff - er.efficiency),
			' || percent_reduction_sql || ' desc, 
			' || get_strategt_cost_sql || '.computed_cost_per_ton';
	END IF;













	-- ALLOWABLE PACKET

	-- next lets caps/replacements, need to union the the various program tables together, so we can make sure and get the most source specific controls
	sql := ''; --reset
	-- see if control strategy program have specific measures or technologies specified
	select count(cptech.id),
		count(cpm.id)
	 from emf.control_strategy_programs csp
		inner join emf.control_programs cp
		on cp.id = csp.control_program_id

		inner join emf.control_program_types cpt
		on cpt.id = cp.control_program_type_id

		left outer join emf.control_program_technologies cptech
		on cptech.control_program_id = cp.id
		
		left outer join emf.control_program_measures cpm
		on cpm.control_program_id = cp.id
		
	where csp.control_strategy_id = control_strategy_id
		and cpt."name" = 'Allowable'
	into control_program_technologies_count, 
		control_program_measures_count;
  	FOR control_program IN EXECUTE 
		'select cp.id as control_program_id, cp."name" as control_program_name, cpt."name" as type, lower(i.table_name) as table_name, 
			cp.start_date, cp.end_date, 
			cp.dataset_id, cp.dataset_version, dt."name" as dataset_type,
			(select count(*) from emf.control_program_technologies cptech where cptech.control_program_id = cp.id) as control_program_technologies_count,
			(select count(*) from emf.control_program_measures cpm where cpm.control_program_id = cp.id) as control_program_measures_count
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
			and cpt."name" = ''Allowable''
			and ''12/31/' || inventory_year || '''::timestamp without time zone between cp.start_date and coalesce(cp.end_date, ''12/31/' || inventory_year || '''::timestamp without time zone)
		order by processing_order'
	LOOP
		-- make sure the dataset type is right...
		IF control_program.dataset_type = 'Allowable Packet' THEN
			--store control dataset version filter in variable
			select public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'proj')
			into control_program_dataset_filter_sql;
			
			If length(sql) > 0 THEN 
				sql := sql || ' union all ';
			END IF;
			--see http://www.smoke-model.org/version2.4/html/ch06s02.html for source matching hierarchy
			sql := sql || '
			select distinct on (record_id, allowable_type)
				record_id,cap,replacement,allowable_type,
				' || quote_literal(control_program.control_program_name) || ' as control_program_name, 
				' || control_program.control_program_technologies_count || ' as control_program_technologies_count, ' || control_program.control_program_measures_count || ' as control_program_measures_count, 
				ranking
			from (
				--placeholder helps dealing with point vs non-point inventories, i dont have to worry about the union all statements
				select 
					null::integer as record_id, null::double precision as cap, null::double precision as replacement, null::varchar(1) as allowable_type, null::integer as ranking
				where 1 = 0

				' || case when is_point_table then '
				--1
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.stackid is not null) and (proj.segment is not null) and (proj.scc is not null) and (proj.poll is not null) then 1::smallint --1
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					and proj.plantid = inv.plantid
					and proj.pointid = inv.pointid
					and proj.stackid = inv.stackid
					and proj.segment = inv.segment
					and proj.scc = inv.scc
					and proj.poll = inv.poll

				where proj.fips is not null 
					and proj.plantid is not null 
					and proj.pointid is not null 
					and proj.stackid is not null 
					and proj.segment is not null 
					and proj.scc is not null 
					and proj.poll is not null
					and proj.sic is null

					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--2
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.stackid is not null) and (proj.segment is not null) and (proj.poll is not null) then 2::smallint --2
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.pointid = inv.pointid
					and proj.stackid = inv.stackid
					and proj.segment = inv.segment
					and proj.poll = inv.poll

				where proj.fips is not null 
					and proj.plantid is not null 
					and proj.pointid is not null 
					and proj.stackid is not null 
					and proj.segment is not null 
					and proj.scc is null 
					and proj.poll is not null
					and proj.sic is null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--3
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.stackid is not null) and (proj.poll is not null) then 3::smallint --3
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.pointid = inv.pointid
					and proj.stackid = inv.stackid
					and proj.poll = inv.poll

				where proj.fips is not null 
					and proj.plantid is not null 
					and proj.pointid is not null 
					and proj.stackid is not null 
					and proj.segment is null 
					and proj.scc is null 
					and proj.poll is not null
					and proj.sic is null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--4
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.poll is not null) then 4::smallint --4
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.pointid = inv.pointid
					and proj.poll = inv.poll

				where proj.fips is not null 
					and proj.plantid is not null 
					and proj.pointid is not null 
					and proj.stackid is null 
					and proj.segment is null 
					and proj.scc is null 
					and proj.poll is not null
					and proj.sic is null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--5
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.scc is not null) and (proj.poll is not null) then 5::smallint --5
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.scc = inv.scc
					and proj.poll = inv.poll

				where proj.fips is not null 
					and proj.plantid is not null 
					and proj.pointid is null 
					and proj.stackid is null 
					and proj.segment is null 
					and proj.scc is not null 
					and proj.poll is not null
					and proj.sic is null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--6
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.poll is not null) then 6::smallint --6
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.poll = inv.poll

				where proj.fips is not null 
					and proj.plantid is not null 
					and proj.pointid is null 
					and proj.stackid is null 
					and proj.segment is null 
					and proj.scc is null 
					and proj.poll is not null
					and proj.sic is null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--7
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.stackid is not null) and (proj.segment is not null) and (proj.scc is not null) then 7::smallint --7
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.pointid = inv.pointid
					and proj.stackid = inv.stackid
					and proj.segment = inv.segment
					and proj.scc = inv.scc

				where proj.fips is not null 
					and proj.plantid is not null 
					and proj.pointid is not null 
					and proj.stackid is not null 
					and proj.segment is not null 
					and proj.scc is not null 
					and proj.poll is null
					and proj.sic is null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--8
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.stackid is not null) and (proj.segment is not null) then 8::smallint --8
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.pointid = inv.pointid
					and proj.stackid = inv.stackid
					and proj.segment = inv.segment

				where proj.fips is not null 
					and proj.plantid is not null 
					and proj.pointid is not null 
					and proj.stackid is not null 
					and proj.segment is not null 
					and proj.scc is null 
					and proj.poll is null
					and proj.sic is null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--9
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) and (proj.stackid is not null) then 9::smallint --9
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.pointid = inv.pointid
					and proj.stackid = inv.stackid

				where proj.fips is not null 
					and proj.plantid is not null 
					and proj.pointid is not null 
					and proj.stackid is not null 
					and proj.segment is null 
					and proj.scc is null 
					and proj.poll is null
					and proj.sic is null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--10
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.pointid is not null) then 10::smallint --10
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.pointid = inv.pointid

				where proj.fips is not null 
					and proj.plantid is not null 
					and proj.pointid is not null 
					and proj.stackid is null 
					and proj.segment is null 
					and proj.scc is null 
					and proj.poll is null
					and proj.sic is null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--11
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) and (proj.scc is not null) then 11::smallint --11
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid
					and proj.scc = inv.scc

				where proj.fips is not null 
					and proj.plantid is not null 
					and proj.pointid is null 
					and proj.stackid is null 
					and proj.segment is null 
					and proj.scc is not null 
					and proj.poll is null
					and proj.sic is null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--12
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.plantid is not null) then 12::smallint --12
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.fips = inv.fips
					
					and proj.plantid = inv.plantid

				where proj.fips is not null 
					and proj.plantid is not null 
					and proj.pointid is null 
					and proj.stackid is null 
					and proj.segment is null 
					and proj.scc is null 
					and proj.poll is null
					and proj.sic is null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone
				' else '' end || '
				
				' || case when has_sic_column then '
				--25,27
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.sic is not null) and (proj.poll is not null) then 25::smallint --25
						when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.sic is not null) and (proj.poll is not null) then 27::smallint --27
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
					and proj.sic = inv.sic
					and proj.poll = inv.poll

				where proj.fips is not null 
					and proj.plantid is null 
					and proj.pointid is null 
					and proj.stackid is null 
					and proj.segment is null 
					and proj.scc is null 
					and proj.poll is not null
					and proj.sic is not null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--29
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.sic is not null) and (proj.poll is not null) then 29::smallint --29
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.sic = inv.sic
					and proj.poll = inv.poll

				where proj.fips is null 
					and proj.plantid is null 
					and proj.pointid is null 
					and proj.stackid is null 
					and proj.segment is null 
					and proj.scc is null 
					and proj.poll is not null
					and proj.sic is not null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--31,33
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.sic is not null) then 31::smallint --31
						when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.sic is not null) then 33::smallint --33
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
					and proj.sic = inv.sic

				where proj.fips is not null 
					and proj.plantid is null 
					and proj.pointid is null 
					and proj.stackid is null 
					and proj.segment is null 
					and proj.scc is null 
					and proj.poll is null
					and proj.sic is not null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--35
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.sic is not null) then 35::smallint --35
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.sic = inv.sic

				where proj.fips is null 
					and proj.plantid is null 
					and proj.pointid is null 
					and proj.stackid is null 
					and proj.segment is null 
					and proj.scc is null 
					and proj.poll is null
					and proj.sic is not null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone
				' else '' end || '

				--37,41
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.scc is not null) and (proj.poll is not null) then 37::smallint --37
						when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.scc is not null) and (proj.poll is not null) then 41::smallint --41
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
					and proj.scc = inv.scc
					and proj.poll = inv.poll

				where proj.fips is not null 
					and proj.plantid is null 
					and proj.pointid is null 
					and proj.stackid is null 
					and proj.segment is null 
					and proj.scc is not null 
					and proj.poll is not null
					and proj.sic is null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--45
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.scc is not null) and (proj.poll is not null) then 45::smallint --45
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.scc = inv.scc
					and proj.poll = inv.poll

				where proj.fips is null 
					and proj.plantid is null 
					and proj.pointid is null 
					and proj.stackid is null 
					and proj.segment is null 
					and proj.scc is not null 
					and proj.poll is not null
					and proj.sic is null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--49,53
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.scc is not null) then 49::smallint --49
						when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.scc is not null) then 53::smallint --53
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
					and proj.scc = inv.scc

				where proj.fips is not null 
					and proj.plantid is null 
					and proj.pointid is null 
					and proj.stackid is null 
					and proj.segment is null 
					and proj.scc is not null 
					and proj.poll is null
					and proj.sic is null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--57
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.scc is not null) then 57::smallint --57
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.scc = inv.scc

				where proj.fips is null 
					and proj.plantid is null 
					and proj.pointid is null 
					and proj.stackid is null 
					and proj.segment is null 
					and proj.scc is not null 
					and proj.poll is null
					and proj.sic is null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--61,63
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) and (proj.poll is not null) then 61::smallint --61
						when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) and (proj.poll is not null) then 63::smallint --63
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))
					and proj.poll = inv.poll

				where proj.fips is not null 
					and proj.plantid is null 
					and proj.pointid is null 
					and proj.stackid is null 
					and proj.segment is null 
					and proj.scc is null 
					and proj.poll is not null
					and proj.sic is null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--62,64
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.fips is not null) and (length(proj.fips) = 5 or length(proj.fips) = 6) then 62::smallint --62
						when (proj.fips is not null) and (length(proj.fips) = 2 or length(proj.fips) = 3) then 64::smallint --64
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on (proj.fips = inv.fips or proj.fips = substr(inv.fips, 1, 2))

				where proj.fips is not null 
					and proj.plantid is null 
					and proj.pointid is null 
					and proj.stackid is null 
					and proj.segment is null 
					and proj.scc is null 
					and proj.poll is null
					and proj.sic is null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

				--65
				union all
				select 
					inv.record_id,proj.cap,proj.replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,
					case 
						when (proj.poll is not null) then 65::smallint --65
					end as ranking
				FROM emissions.' || control_program.table_name || ' proj
					inner join emissions.' || inv_table_name || ' inv
							
					on proj.poll = inv.poll

				where proj.fips is null 
					and proj.plantid is null 
					and proj.pointid is null 
					and proj.stackid is null 
					and proj.segment is null 
					and proj.scc is null 
					and proj.poll is not null
					and proj.sic is null
					and ' || control_program_dataset_filter_sql || '
					and ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					-- make the compliance date has been met
					and coalesce(proj.compliance_date, ''1/1/' || inventory_year || '''::timestamp without time zone) >= ''1/1/' || inventory_year || '''::timestamp without time zone

			) tbl';
		END IF;

	END LOOP;
--		raise notice '%', sql;

	IF length(sql) > 0 THEN
		raise notice '%', 'next lets do caps and replacemnts ' || clock_timestamp();
		sql := sql || ' order by record_id, allowable_type, ranking';

		uncontrolled_emis_sql := 
				case 
					when dataset_month != 0 then 
						'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then case when cdr.record_id is not null then cdr.final_emissions when pdr.record_id is not null then pdr.final_emissions else coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) end / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
					else 
						'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then case when cdr.record_id is not null then cdr.final_emissions when pdr.record_id is not null then pdr.final_emissions else inv.ann_emis end / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
				end;
		emis_sql := 
				case 
					when dataset_month != 0 then 
						'case when cdr.record_id is not null then cdr.final_emissions when pdr.record_id is not null then pdr.final_emissions else coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) end' 
					else 
						'case when cdr.record_id is not null then cdr.final_emissions when pdr.record_id is not null then pdr.final_emissions else inv.ann_emis end' 
				end;
		annualized_uncontrolled_emis_sql := 
				case 
					when dataset_month != 0 then 
						'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then case when cdr.record_id is not null then cdr.final_emissions when pdr.record_id is not null then pdr.final_emissions else coalesce(inv.avd_emis * 365, inv.ann_emis) end / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
					else 
						'case when (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) != 0 then case when cdr.record_id is not null then cdr.final_emissions when pdr.record_id is not null then pdr.final_emissions else inv.ann_emis end / (1 - coalesce(inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
				end;

		percent_reduction_sql := 'cont.ceff * coalesce(cont.reff, 100) * coalesce(cont.rpen, 100) / 100 / 100';
		get_strategt_cost_sql := '(public.get_strategy_costs(' || case when use_cost_equations then 'true' else 'false' end || '::boolean, m.id, 
				abbreviation, ' || discount_rate|| ', 
				m.equipment_life, er.cap_ann_ratio, 
				er.cap_rec_factor, er.ref_yr_cost_per_ton, 
				' || uncontrolled_emis_sql || ' * ' || percent_reduction_sql || ' / 100, ' || ref_cost_year_chained_gdp || ' / cast(chained_gdp as double precision), 
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


		-- make sure the apply order is 4,....this is important when the controlled inventpory is created.
		-- the apply order will dictate how the controlled inventpory is created
		execute 
		--raise notice '%', 
		'insert into emissions.' || detailed_result_table_name || ' 
			(
			dataset_id,
			cm_abbrev,
			poll,
			scc,
			fips,
			' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '
			annual_oper_maint_cost,
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
			adj_factor,
			final_emissions,
			emis_reduction,
			inv_emissions,
	--				input_emis,
	--				output_emis,
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
			control_program,
			xloc,
			yloc,
			plant,
			"comment"
			)
		select distinct on (inv.record_id, allowable_type)

			' || detailed_result_dataset_id || '::integer,
			coalesce(case when case when ' || emis_sql || ' <> 0 then coalesce(cont.replacement, cont.cap) * 365 / ' || emis_sql || ' else 0.0::double precision end <> 0 and abs(case when ' || emis_sql || ' <> 0 then coalesce(cont.replacement, cont.cap) * 365 / ' || emis_sql || ' else null::double precision end * 100.0 - er.efficiency) / case when ' || emis_sql || ' <> 0 then coalesce(cont.replacement, cont.cap) * 365 / ' || emis_sql || ' else null::double precision end * 100 * 100 <= ' || replacement_control_min_eff_diff_constraint || ' then m.abbreviation else null::character varying end, ''UNKNOWNMSR'') as abbreviation,
--			case when allowable_type = ''C'' then ''C'' || substring(m.abbreviation,2,length(m.abbreviation)) when allowable_type = ''R'' then ''R'' || substring(m.abbreviation,2,length(m.abbreviation)) end as abbreviation,
			inv.poll,
			inv.scc,
			inv.fips,
			' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
			null::double precision as operation_maintenance_cost,
			null::double precision as annualized_capital_cost,
			null::double precision as capital_cost,
			null::double precision as ann_cost,
			null::double precision as computed_cost_per_ton,
			case when ' || emis_sql || ' <> 0 then coalesce(cont.replacement, cont.cap) * 365 / ' || emis_sql || ' else null::double precision end * 100.0 as control_eff,
			null::double precision as rule_pen,
			null::double precision as rule_eff,
			null::double precision as percent_reduction,
			inv.ceff,
			' || case when is_point_table = false then 'inv.rpen' else '100' end || ',
			inv.reff,
			case when ' || emis_sql || ' <> 0 then coalesce(cont.replacement, cont.cap) * 365 / ' || emis_sql || ' else null::double precision end as adj_factor,
			case when allowable_type = ''C'' then cont.cap * 365 when allowable_type = ''R'' then cont.replacement * 365 end as final_emissions,
			' || emis_sql || ' - (case when allowable_type = ''C'' then cont.cap * 365 when allowable_type = ''R'' then cont.replacement * 365 end) as emis_reduction,
			' || emis_sql || ' as inv_emissions,
	--				' || emis_sql || ' as input_emis,
	--				0.0 as output_emis,
			case when allowable_type = ''C'' then 4 when allowable_type = ''R'' then 5 end as apply_order,
			substr(inv.fips, 1, 2),
			substr(inv.fips, 3, 3),
			' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ',
			' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ',
			inv.record_id::integer as source_id,
			' || input_dataset_id || '::integer,
			' || control_strategy_id || '::integer,
			null::integer as control_measures_id,
			null::varchar(255) as equation_type,
			cont.control_program_name,
			' || case when has_latlong_columns then 'inv.xloc,inv.yloc,' else 'fipscode.centerlon as xloc,fipscode.centerlat as yloc,' end || '
			' || case when has_plant_column then 'inv.plant' when not has_latlong_columns then 'fipscode.state_county_fips_code_desc as plant' else 'null::character varying as plant' end || ',
			''''

		FROM emissions.' || inv_table_name || ' inv

			-- see if the source was controlled, if so, use the controlled values
			left outer join emissions.' || detailed_result_table_name || ' cdr
			on cdr.source_id = inv.record_id
			and cdr.input_ds_id = inv.dataset_id
			and cdr.apply_order = 2

			-- see if the source was projected, if so, use the projected values, controlled values will override the projected values, if there are both...
			left outer join emissions.' || detailed_result_table_name || ' pdr
			on pdr.source_id = inv.record_id
			and pdr.input_ds_id = inv.dataset_id
			and pdr.apply_order = 1

			inner join emf.pollutants p
			on p.name = inv.poll
			
			' || case when not has_latlong_columns then 'left outer join reference.fips fipscode
			on fipscode.state_county_fips = inv.fips
			and fipscode.country_num = ''0''' else '' end || '

			inner join (' || sql || ') cont
			on cont.record_id = inv.record_id




			-- tables for predicting measure
			left outer join emf.control_measure_efficiencyrecords er
			-- poll filter
			on er.pollutant_id = p.id
			-- min and max emission filter
			and coalesce(cont.replacement, cont.cap) * 365 between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
			-- locale filter
			and (er.locale = inv.fips or er.locale = substr(inv.fips, 1, 2) or er.locale = '''')
			-- effecive date filter
			and ' || inventory_year || '::integer >= coalesce(date_part(''year'', er.effective_date), ' || inventory_year || '::integer)		


			left outer join emf.control_measures m
			on m.id = er.control_measures_id
			

			left outer join emf.control_measure_equations eq
			on eq.control_measure_id = m.id
			and eq.pollutant_id = p.id

			left outer join emf.equation_types et
			on et.id = eq.equation_type_id

			left outer join reference.gdplev
			on gdplev.annual = eq.cost_year

		where 	' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
			and (
				(allowable_type = ''C'' and ' || emis_sql || ' >= cont.cap * 365)
				or (allowable_type = ''R'')
			)
			-- control program measure and technology filter
			' || case when control_program_measures_count > 0 and control_program_technologies_count > 0  then '
			and (
				(cont.control_program_measures_count > 0 and exists (select 1 from emf.control_program_measures cpm where cpm.control_measure_id = m.id))
				or 
				(cont.control_program_technologies_count > 0 and exists (select 1 from emf.control_program_technologies cpt where cpt.control_program_id = cont.control_program_id and cpt.control_technology_id = m.control_technology))
			)
			' when control_program_measures_count > 0 then '
			and cont.control_program_measures_count > 0 and exists (select 1 from emf.control_program_measures cpm where cpm.control_measure_id = m.id)
			' when control_program_technologies_count > 0 then '
			and cont.control_program_technologies_count > 0 and exists (select 1 from emf.control_program_technologies cpt where cpt.control_program_id = cont.control_program_id and cpt.control_technology_id = m.control_technology)
			' else '' end || '
--			and cont.ceff >= coalesce(inv.ceff, 0.0)
--			and ' || emis_sql || ' <> 0

		order by inv.record_id, allowable_type,
			case when length(er.locale) = 5 then 0 when length(er.locale) = 2 then 1 else 2 end, 	
			abs(case when ' || emis_sql || ' <> 0 then coalesce(cont.replacement, cont.cap) * 365 / ' || emis_sql || ' else 0.0::double precision end - er.efficiency)';
		END IF;

END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
