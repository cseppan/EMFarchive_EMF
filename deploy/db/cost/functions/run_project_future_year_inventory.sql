/*
select public.run_project_future_year_inventory(
	153, --int_control_strategy_id, 
	349, --input_dataset_id, 
	1, --input_dataset_version, 
	6117 --strategy_result_id
	);
SELECT public.run_project_future_year_inventory(137, 5048, 0, 6147);
*/

DROP FUNCTION public.run_project_future_year_inventory(integer, integer, integer, integer);

CREATE OR REPLACE FUNCTION public.run_project_future_year_inventory(
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
	inventory_year smallint := null;
	is_point_table boolean := false;
	use_cost_equations boolean := false;
	gimme_count integer := 0;
	min_emis_reduction_constraint real := null;
	min_control_efficiency_constraint real := null;
	max_cost_per_ton_constraint real := null;
	max_ann_cost_constraint real := null;
	replacement_control_min_eff_diff_constraint double precision := null;
	control_program_measure_min_pct_red_diff_constraint double precision := null;
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
	cont_packet_percent_reduction_sql character varying;
	inv_percent_reduction_sql character varying;
	insert_column_list_sql character varying := '';
	select_column_list_sql character varying := '';
	column_name character varying;
	get_strategt_cost_sql character varying;
	get_strategt_cost_inner_sql character varying;
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
	into detailed_result_dataset_id,
		detailed_result_table_name;

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
--		case when length(trim(cs.filter)) > 0 then '(' || public.alias_inventory_filter(cs.filter, 'inv') || ')' else null end,
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

	-- see if there is lat & long columns in the inventory
	has_latlong_columns := public.check_table_for_columns(inv_table_name, 'xloc,yloc', ',');

	-- see if there is lat & long columns in the inventory
	has_plant_column := public.check_table_for_columns(inv_table_name, 'plant', ',');


	-- get strategy constraints
	SELECT max_emis_reduction,
		max_control_efficiency,
		min_cost_per_ton,
		min_ann_cost,
		replacement_control_min_eff_diff,
		control_program_measure_min_pct_red_diff
	FROM emf.control_strategy_constraints csc
	where csc.control_strategy_id = int_control_strategy_id
	INTO min_emis_reduction_constraint,
		min_control_efficiency_constraint,
		max_cost_per_ton_constraint,
		max_ann_cost_constraint,
		replacement_control_min_eff_diff_constraint,
		control_program_measure_min_pct_red_diff_constraint;

	select case when min_emis_reduction_constraint is not null 
		or min_control_efficiency_constraint is not null 
		or max_cost_per_ton_constraint is not null 
		or max_ann_cost_constraint is not null then true else false end
	into has_constraints;

	-- get month of the dataset, 0 (Zero) indicates an annual inventory
	select public.get_dataset_month(input_dataset_id)
	into dataset_month;

	select public.get_days_in_month(dataset_month, inventory_year)
	into no_days_in_month;

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
	
	uncontrolled_emis_sql := 
			case 
				when dataset_month != 0 then 
					'case when (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) / (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
				else 
					'case when (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) != 0 then inv.ann_emis / (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
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
					'case when (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(inv.avd_emis * 365, inv.ann_emis) / (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
				else 
					'case when (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) != 0 then inv.ann_emis / (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
			end;






	-- see if their was a county dataset specified for the strategy, is so then build a sql where clause filter for later use
	IF county_dataset_id is not null THEN
		county_dataset_filter_sql := ' and inv.fips in (SELECT fips
			FROM emissions.' || (SELECT table_name FROM emf.internal_sources where dataset_id = county_dataset_id) || '
			where ' || public.build_version_where_filter(county_dataset_id, county_dataset_version) || ')';
	END IF;
	-- build version info into where clause filter
--	inv_filter := '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'inv') || ')' || coalesce(' and ' || inv_filter, '');


--	inv_filter := '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version) || ')' || coalesce(' and ' || inv_filter, '');

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
		where csp.control_strategy_id = ' || int_control_strategy_id || '
			and cpt."name" = ''Plant Closure''
			and ''12/31/' || inventory_year || '''::timestamp without time zone between cp.start_date and coalesce(cp.end_date, ''12/31/' || inventory_year || '''::timestamp without time zone)
		order by processing_order'
	LOOP
		-- apply plant closure control program
		IF control_program.type = 'Plant Closure' THEN
raise notice '%', public.alias_filter(inv_filter, inv_table_name, 'inv'::character varying(64));
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
			-- add distinct to limit to only one closure record, that is all that is needed...
			select distinct on (record_id)
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
				' || case when is_point_table = false then 'case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end' else '100' end || ',
				case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end,
				0.0 as final_emissions,
				' || emis_sql || ' as emis_reduction,
				' || emis_sql || ' as inv_emissions,
--				' || emis_sql || ' as input_emis,
--				0.0 as output_emis,
				0,
				substr(inv.fips, 1, 2),
				substr(inv.fips, 3, 3),
				' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ',
				' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ',
				inv.record_id::integer as source_id,
				' || input_dataset_id || '::integer,
				' || int_control_strategy_id || '::integer,
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

				-- only keep if before cutoff date
				and pc.effective_date::timestamp without time zone < ''' || effective_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone

				and ' || public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'pc') || '

				' || case when not has_latlong_columns then 'left outer join reference.fips fipscode
				on fipscode.state_county_fips = inv.fips
				and fipscode.country_num = ''0''' else '' end || '

			where 	' || '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'inv') || ')' || coalesce(' and ' || public.alias_filter(inv_filter, inv_table_name, 'inv'::character varying(64)), '') || coalesce(county_dataset_filter_sql, '') || '
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
		where csp.control_strategy_id = ' || int_control_strategy_id || '
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

				' || public.build_project_future_year_inventory_matching_hierarchy_sql(
				input_dataset_id, --inv_dataset_id integer, 
				input_dataset_version, --inv_dataset_version integer, 
				control_program.dataset_id, --control_program_dataset_id integer, 
				control_program.dataset_version, --control_program_dataset_version integer, 
				'proj_factor', --select_columns varchar, 
				inv_filter, --inv_filter text, --not aliased
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
				) || '
				order by record_id, ranking
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
		sql := 'select distinct on (record_id)
				record_id,proj_factor,
				control_program_name, ranking
			from (' || sql;
		sql := sql || ') tbl order by record_id, ranking';

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
			' || case when is_point_table = false then 'case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end' else '100' end || ',
			case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end,
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
			' || int_control_strategy_id || '::integer,
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

		where 	

			--remove plant closures from consideration
			inv.record_id not in (select source_id from emissions.' || detailed_result_table_name || ' where apply_order = 0)';
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
		
	where csp.control_strategy_id = int_control_strategy_id
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

		where csp.control_strategy_id = ' || int_control_strategy_id || '
			and cpt."name" = ''Control''
			and ''12/31/' || inventory_year || '''::timestamp without time zone between cp.start_date and coalesce(cp.end_date, ''12/31/' || inventory_year || '''::timestamp without time zone)
		order by processing_order'
	LOOP
		-- make sure the dataset type is right...
		IF control_program.dataset_type = 'Control Packet' THEN
			--store control dataset version filter in variable
			select public.build_version_where_filter(control_program.dataset_id, control_program.dataset_version, 'proj')
			into control_program_dataset_filter_sql;
			
			If length(sql) > 0 THEN 
				sql := sql || ' union all ';
			END IF;
			--see http://www.smoke-model.org/version2.4/html/ch06s02.html for source matching hierarchy
			sql := sql || '
			select 
				--distinct on (record_id)
				record_id,ceff,rpen,reff,pri_cm_abbrev,replacement,compliance_date,
				' || quote_literal(control_program.control_program_name) || ' as control_program_name, ' || control_program.control_program_id || ' as control_program_id,
				' || control_program.control_program_technologies_count || ' as control_program_technologies_count, ' || control_program.control_program_measures_count || ' as control_program_measures_count, 
				ranking
			from (

				' || 
		public.build_project_future_year_inventory_matching_hierarchy_sql(
			input_dataset_id, --inv_dataset_id integer, 
			input_dataset_version, --inv_dataset_version integer, 
			control_program.dataset_id, --control_program_dataset_id integer, 
			control_program.dataset_version, --control_program_dataset_version integer, 
			'ceff,rpen,reff,pri_cm_abbrev,replacement,compliance_date', --select_columns varchar, 
			inv_filter, --inv_filter text,
			county_dataset_id, --1279 county_dataset_id integer,
			county_dataset_version, --county_dataset_version integer,
			'application_control = ''Y'' and coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) < ''' || compliance_date_cutoff_daymonth || '/' || inventory_year || '''::timestamp without time zone',
			1 --match_type integer	-- 1 = include only Matched Sources, 2 = Include packet records that didn't affect a source, 3 = ?
			)


				|| '

				--order by record_id, replacement, ranking, coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) desc
			 ) tbl';
		END IF;

	END LOOP;
--		raise notice '%', sql;



	IF length(sql) > 0 THEN
		raise notice '%', 'next lets do controls ' || clock_timestamp();
		sql := 'select distinct on (record_id)
				record_id,ceff,rpen,reff,pri_cm_abbrev,replacement,compliance_date,
				control_program_name, control_program_id,
				control_program_technologies_count, control_program_measures_count, 
				ranking
			from (' || sql;
		sql := sql || ') tbl order by record_id, ranking, replacement, coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) desc';

		inv_percent_reduction_sql := '(coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end, 0.0) * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end, 100) / 100 ' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end, 100.0) / 100.0 ' else '' end || ')';
		cont_packet_percent_reduction_sql := '(cont.ceff * coalesce(cont.reff, 100) / 100 * coalesce(cont.rpen, 100) / 100)';
		uncontrolled_emis_sql := 
				case 
					when dataset_month != 0 then 
						'case when (1 - ' || inv_percent_reduction_sql || ' / 100) != 0 then case when dr.record_id is not null then dr.final_emissions else coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) end / (1 - coalesce(' || inv_percent_reduction_sql || ' / 100.0, 0)) else 0.0::double precision end' 
					else 
						'case when (1 - ' || inv_percent_reduction_sql || ' / 100) != 0 then case when dr.record_id is not null then dr.final_emissions else inv.ann_emis end / (1 - coalesce(' || inv_percent_reduction_sql || ' / 100.0, 0)) else 0.0::double precision end' 
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
						'case when (1 - coalesce(' || inv_percent_reduction_sql || ' / 100, 0)) != 0 then case when dr.record_id is not null then dr.final_emissions else coalesce(inv.avd_emis * 365, inv.ann_emis) end / (1 - coalesce(' || inv_percent_reduction_sql || ' / 100.0, 0)) else 0.0::double precision end'
					else 
						'case when (1 - coalesce(' || inv_percent_reduction_sql || ' / 100, 0)) != 0 then case when dr.record_id is not null then dr.final_emissions else inv.ann_emis end / (1 - coalesce(' || inv_percent_reduction_sql || ' / 100.0, 0)) else 0.0::double precision end'
				end;



		get_strategt_cost_sql := '(public.get_strategy_costs(' || case when use_cost_equations then 'true' else 'false' end || '::boolean, m.id, 
				abbreviation, ' || discount_rate|| ', 
				m.equipment_life, er.cap_ann_ratio, 
				er.cap_rec_factor, er.ref_yr_cost_per_ton, 
				case 
					when cont.replacement = ''R'' then ' || uncontrolled_emis_sql || ' * ' || cont_packet_percent_reduction_sql || ' / 100.0
					when cont.replacement = ''A'' then ' || emis_sql || ' * ' || cont_packet_percent_reduction_sql || ' / 100.0
				end
				, ' || ref_cost_year_chained_gdp || ' / cast(gdplev.chained_gdp as double precision), 
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
				|| ',inv.ceff, ' || ref_cost_year_chained_gdp || '::double precision / gdplev_incr.chained_gdp::double precision * er.incremental_cost_per_ton))';


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
			coalesce(cont.pri_cm_abbrev, case when cont.ceff <> 0 and abs(' || cont_packet_percent_reduction_sql || ' - er.efficiency * coalesce(er.rule_effectiveness, 100) / 100.0 * coalesce(er.rule_penetration, 100) / 100.0) / ' || cont_packet_percent_reduction_sql || ' <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision / 100.0 then m.abbreviation else null::character varying end, ''UNKNOWNMSR'') as abbreviation,
			inv.poll,
			inv.scc,
			inv.fips,
			' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
			case when cont.pri_cm_abbrev is null and er.id is not null and cont.ceff <> 0 and abs(' || cont_packet_percent_reduction_sql || ' - er.efficiency * coalesce(er.rule_effectiveness, 100) / 100.0 * coalesce(er.rule_penetration, 100) / 100.0) / ' || cont_packet_percent_reduction_sql || ' <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision / 100.0 then ' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.operation_maintenance_cost else null::double precision end as operation_maintenance_cost,
			case when cont.pri_cm_abbrev is null and er.id is not null and cont.ceff <> 0 and abs(' || cont_packet_percent_reduction_sql || ' - er.efficiency * coalesce(er.rule_effectiveness, 100) / 100.0 * coalesce(er.rule_penetration, 100) / 100.0) / ' || cont_packet_percent_reduction_sql || ' <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision / 100.0 then ' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.annualized_capital_cost else null::double precision end as annualized_capital_cost,
			case when cont.pri_cm_abbrev is null and er.id is not null and cont.ceff <> 0 and abs(' || cont_packet_percent_reduction_sql || ' - er.efficiency * coalesce(er.rule_effectiveness, 100) / 100.0 * coalesce(er.rule_penetration, 100) / 100.0) / ' || cont_packet_percent_reduction_sql || ' <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision / 100.0 then ' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.capital_cost else null::double precision end as capital_cost,
			case when cont.pri_cm_abbrev is null and er.id is not null and cont.ceff <> 0 and abs(' || cont_packet_percent_reduction_sql || ' - er.efficiency * coalesce(er.rule_effectiveness, 100) / 100.0 * coalesce(er.rule_penetration, 100) / 100.0) / ' || cont_packet_percent_reduction_sql || ' <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision / 100.0 then ' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.annual_cost else null::double precision end as ann_cost,
			case when cont.pri_cm_abbrev is null and er.id is not null and cont.ceff <> 0 and abs(' || cont_packet_percent_reduction_sql || ' - er.efficiency * coalesce(er.rule_effectiveness, 100) / 100.0 * coalesce(er.rule_penetration, 100) / 100.0) / ' || cont_packet_percent_reduction_sql || ' <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision / 100.0 then ' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.computed_cost_per_ton else null::double precision end as computed_cost_per_ton,
			case 
				when cont.replacement = ''R'' then cont.ceff 
				when cont.replacement = ''A'' then ' || inv_percent_reduction_sql || ' + ' || cont_packet_percent_reduction_sql || ' * ( 100.0 - ' || inv_percent_reduction_sql || ') / 100.0
			end as control_eff,
			case 
				when cont.replacement = ''R'' then coalesce(cont.rpen, 100)
				when cont.replacement = ''A'' then 100.0
			end as rule_pen,
			case 
				when cont.replacement = ''R'' then coalesce(cont.reff, 100)
				when cont.replacement = ''A'' then 100.0
			end as rule_eff,
			case 
				when cont.replacement = ''R'' then ' || cont_packet_percent_reduction_sql || '
				when cont.replacement = ''A'' then ' || inv_percent_reduction_sql || ' + ' || cont_packet_percent_reduction_sql || ' * ( 100.0 - ' || inv_percent_reduction_sql || ') / 100.0
			end as percent_reduction,
			inv.ceff as inv_ceff,
			' || case when is_point_table = false then 'case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end' else '100' end || ' as inv_rpen,
			case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end as inv_reff,
			' || inv_percent_reduction_sql || ' / 100 as inv_pct_red,
			case 
				when cont.replacement = ''R'' then 
					case 
						when cont.ceff <> 0 then 
						' || uncontrolled_emis_sql || ' * (1 - ' || cont_packet_percent_reduction_sql || ' / 100) 
						else ' || emis_sql || ' 
					end 
				when cont.replacement = ''A'' then ' || emis_sql || ' * (1 - ' || cont_packet_percent_reduction_sql || ' / 100)
			end as final_emissions,
			case 
				when cont.replacement = ''R'' then 
					case 
						when cont.ceff <> 0 then ' || uncontrolled_emis_sql || ' * ' || cont_packet_percent_reduction_sql || ' / 100 
						else 0.0 
					end 
				when cont.replacement = ''A'' then ' || emis_sql || ' * ' || cont_packet_percent_reduction_sql || ' / 100
			end as emis_reduction,
			' || emis_sql || '  as inv_emissions,
--				' || uncontrolled_emis_sql || ' as input_emis,
--				0.0 as output_emis,
			2,
			substr(inv.fips, 1, 2),
			substr(inv.fips, 3, 3),
			' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ',
			' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ',
			inv.record_id::integer as source_id,
			' || input_dataset_id || '::integer,
			' || int_control_strategy_id || '::integer,
			null::integer as control_measures_id,
			case when cont.pri_cm_abbrev is null and er.id is not null and cont.ceff <> 0 and abs(' || cont_packet_percent_reduction_sql || ' - er.efficiency * coalesce(er.rule_effectiveness, 100) / 100.0 * coalesce(er.rule_penetration, 100) / 100.0) / ' || cont_packet_percent_reduction_sql || ' <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision / 100.0  then ' || get_strategt_cost_sql || '.actual_equation_type else null::varchar(255) end as equation_type,
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

			left outer join emf.pollutants p
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
			and abs(er.efficiency - cont.ceff) <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision
			and abs(' || cont_packet_percent_reduction_sql || ' - er.efficiency * coalesce(er.rule_effectiveness, 100) / 100.0 * coalesce(er.rule_penetration, 100) / 100.0) / ' || cont_packet_percent_reduction_sql || ' <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision / 100.0
--			and ' || cont_packet_percent_reduction_sql || ' <> 0.0
			
			left outer join reference.gdplev gdplev_incr
			on gdplev_incr.annual = er.cost_year

			left outer join emf.control_measures m
			on m.id = er.control_measures_id
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
			

			left outer join emf.control_measure_equations eq
			on eq.control_measure_id = m.id
			and eq.pollutant_id = p.id

			left outer join emf.equation_types et
			on et.id = eq.equation_type_id

			left outer join reference.gdplev
			on gdplev.annual = eq.cost_year

		where 	(
				(cont.replacement = ''R'' 
					and (
						(' || cont_packet_percent_reduction_sql || ' >= ' || inv_percent_reduction_sql || ')
						or cont.ceff = 0.0
					)
				)
				or (cont.replacement = ''A'')
			)

			--remove plant closures from consideration
			and inv.record_id not in (select source_id from emissions.' || detailed_result_table_name || ' where apply_order = 0)
		order by inv.record_id, 
			--makes sure we get the highest ranking control packet record
			cont.ranking,
			--makes sure replacements trump add on controls
			replacement, 
			case when length(er.locale) = 5 then 0 when length(er.locale) = 2 then 1 else 2 end, 	
			cont.ceff - er.efficiency,
			' || cont_packet_percent_reduction_sql || ' desc, 
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
		
	where csp.control_strategy_id = int_control_strategy_id
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

		where csp.control_strategy_id = ' || int_control_strategy_id || '
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
			select 
				--distinct on (record_id, allowable_type)
				record_id,cap,replacement,allowable_type,compliance_date,
				' || quote_literal(control_program.control_program_name) || ' as control_program_name, 
				' || control_program.control_program_technologies_count || ' as control_program_technologies_count, ' || control_program.control_program_measures_count || ' as control_program_measures_count, 
				ranking
			from (
				' || public.build_project_future_year_inventory_matching_hierarchy_sql(
				input_dataset_id, --inv_dataset_id integer, 
				input_dataset_version, --inv_dataset_version integer, 
				control_program.dataset_id, --control_program_dataset_id integer, 
				control_program.dataset_version, --control_program_dataset_version integer, 
				'cap,replacement,case when replacement is not null then ''R'' when cap is not null then ''C'' end as allowable_type,compliance_date', --select_columns varchar, 
				inv_filter, --inv_filter text, --not aliased
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
				) || '


				--order by record_id, allowable_type, ranking, compliance_date desc
			) tbl';
		END IF;

	END LOOP;
--		raise notice '%', sql;

	IF length(sql) > 0 THEN
		raise notice '%', 'next lets do caps and replacemnts ' || clock_timestamp();

		sql := 'select distinct on (record_id, allowable_type)
				record_id,cap,replacement,allowable_type,
				control_program_name, 
				control_program_technologies_count, control_program_measures_count, 
				ranking
			from (' || sql;
		sql := sql || ') tbl order by record_id, allowable_type, ranking, coalesce(compliance_date, ''1/1/1900''::timestamp without time zone) desc';

		uncontrolled_emis_sql := 
				case 
					when dataset_month != 0 then 
						'case when (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) != 0 then case when cdr.record_id is not null then cdr.final_emissions when pdr.record_id is not null then pdr.final_emissions else coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) end / (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
					else 
						'case when (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) != 0 then case when cdr.record_id is not null then cdr.final_emissions when pdr.record_id is not null then pdr.final_emissions else inv.ann_emis end / (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
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
						'case when (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) != 0 then case when cdr.record_id is not null then cdr.final_emissions when pdr.record_id is not null then pdr.final_emissions else coalesce(inv.avd_emis * 365, inv.ann_emis) end / (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
					else 
						'case when (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) != 0 then case when cdr.record_id is not null then cdr.final_emissions when pdr.record_id is not null then pdr.final_emissions else inv.ann_emis end / (1 - coalesce(case when inv.ceff = 100.0 and coalesce(inv.avd_emis, inv.ann_emis) > 0.0 then 0.0 else inv.ceff end / 100 * coalesce(case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end / 100, 1.0)' || case when has_rpen_column then ' * coalesce(case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
				end;


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
		select distinct on (inv.record_id, cont.allowable_type)

			' || detailed_result_dataset_id || '::integer,
			coalesce(case when case when ' || emis_sql || ' <> 0 then coalesce(cont.replacement, cont.cap) * 365 / ' || emis_sql || ' else 0.0::double precision end <> 0 and abs(case when ' || emis_sql || ' <> 0 then coalesce(cont.replacement, cont.cap) * 365 / ' || emis_sql || ' else null::double precision end * 100.0 - er.efficiency) / case when ' || emis_sql || ' <> 0 then coalesce(cont.replacement, cont.cap) * 365 / ' || emis_sql || ' else null::double precision end * 100 * 100 <= ' || control_program_measure_min_pct_red_diff_constraint || ' then m.abbreviation else null::character varying end, ''UNKNOWNMSR'') as abbreviation,
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
			' || case when is_point_table = false then 'case when inv.rpen = 0.0 and inv.ceff > 0.0 then 100.0 else inv.rpen end' else '100' end || ',
			case when inv.reff = 0.0 and inv.ceff > 0.0 then 100.0 else inv.reff end,
			case when ' || emis_sql || ' <> 0 then coalesce(cont.replacement, cont.cap) * 365 / ' || emis_sql || ' else null::double precision end as adj_factor,
			case when allowable_type = ''C'' then cont.cap * 365 when allowable_type = ''R'' then cont.replacement * 365 end as final_emissions,
			' || emis_sql || ' - (case when allowable_type = ''C'' then cont.cap * 365 when allowable_type = ''R'' then cont.replacement * 365 end) as emis_reduction,
			' || emis_sql || ' as inv_emissions,
	--				' || emis_sql || ' as input_emis,
	--				0.0 as output_emis,
			case when allowable_type = ''C'' then 3 when allowable_type = ''R'' then 4 end as apply_order,
			substr(inv.fips, 1, 2),
			substr(inv.fips, 3, 3),
			' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ',
			' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ',
			inv.record_id::integer as source_id,
			' || input_dataset_id || '::integer,
			' || int_control_strategy_id || '::integer,
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

			left outer join emf.pollutants p
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

			-- tables for predicting measure
			left outer join emf.control_measure_efficiencyrecords er
			on er.control_measures_id = sccs.control_measures_id
			-- poll filter
			and er.pollutant_id = p.id
			-- min and max emission filter
			and coalesce(cont.replacement, cont.cap) * 365 between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
			-- locale filter
			and (er.locale = inv.fips or er.locale = substr(inv.fips, 1, 2) or er.locale = '''')
			-- effecive date filter
			and ' || inventory_year || '::integer >= coalesce(date_part(''year'', er.effective_date), ' || inventory_year || '::integer)
			and abs(er.efficiency - case when ' || emis_sql || ' <> 0 then coalesce(cont.replacement, cont.cap) * 365 / ' || emis_sql || ' else 0.0::double precision end) <= ' || control_program_measure_min_pct_red_diff_constraint || '::double precision

			left outer join emf.control_measures m
			on m.id = er.control_measures_id
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

			left outer join emf.control_measure_equations eq
			on eq.control_measure_id = m.id
			and eq.pollutant_id = p.id

			left outer join emf.equation_types et
			on et.id = eq.equation_type_id

			left outer join reference.gdplev
			on gdplev.annual = eq.cost_year

		where 	
--			and (
--				(allowable_type = ''C'' and ' || emis_sql || ' >= cont.cap * 365)
--				or (allowable_type = ''R'')
--			)

--			and cont.ceff >= coalesce(inv.ceff, 0.0)
--			and ' || emis_sql || ' <> 0

			--remove plant closures from consideration
			inv.record_id not in (select source_id from emissions.' || detailed_result_table_name || ' where apply_order = 0)

		order by inv.record_id, 
			--makes sure we get the highest ranking control packet record
			cont.allowable_type,
			cont.ranking,
			case when length(er.locale) = 5 then 0 when length(er.locale) = 2 then 1 else 2 end, 	
			er.efficiency - case when ' || emis_sql || ' <> 0 then coalesce(cont.replacement, cont.cap) * 365 / ' || emis_sql || ' else 0.0::double precision end';
	END IF;

END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
