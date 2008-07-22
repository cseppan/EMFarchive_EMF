CREATE OR REPLACE FUNCTION public.run_max_emis_red_strategy(control_strategy_id integer, input_dataset_id integer, 
	input_dataset_version integer, strategy_result_id int) RETURNS void AS $$
DECLARE
	inv_table_name varchar(64) := '';
	inv_filter varchar := '';
	inv_fips_filter text := '';
	detailed_result_dataset_id integer := null;
	detailed_result_table_name varchar(64) := '';
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
	has_target_pollutant integer := 0;
	get_strategt_cost_sql character varying;
	has_rpen_column boolean := false;
	get_strategt_cost_inner_sql character varying;
	annualized_emis_sql character varying;
	annual_emis_sql character varying;
	percent_reduction_sql character varying;
BEGIN
	SET work_mem TO '256MB';
--	SET enable_seqscan TO 'off';


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
	SELECT count(1) = 4
	FROM pg_class c
		inner join pg_attribute a
		on a.attrelid = c.oid
		inner join pg_type t
		on t.oid = a.atttypid
	WHERE c.relname = inv_table_name
		and a.attname in ('plantid','pointid','stackid','segment')
		AND a.attnum > 0
	into is_point_table;

	-- see if there is a sic column in the inventory
	SELECT count(1) = 1
	FROM pg_class c
		inner join pg_attribute a
		on a.attrelid = c.oid
		inner join pg_type t
		on t.oid = a.atttypid
	WHERE c.relname = inv_table_name
		and a.attname = 'sic'
		AND a.attnum > 0
	into has_sic_column;

	-- see if there is a naics column in the inventory
	SELECT count(1) = 1
	FROM pg_class c
		inner join pg_attribute a
		on a.attrelid = c.oid
		inner join pg_type t
		on t.oid = a.atttypid
	WHERE c.relname = inv_table_name
		and a.attname = 'naics'
		AND a.attnum > 0
	into has_naics_column;

	-- see if there is a rpen column in the inventory
	SELECT count(1) = 1
	FROM pg_class c
		inner join pg_attribute a
		on a.attrelid = c.oid
		inner join pg_type t
		on t.oid = a.atttypid
	WHERE c.relname = inv_table_name
		and a.attname = 'rpen'
		AND a.attnum > 0
	into has_rpen_column;

	SELECT count(1) = 3
	FROM pg_class c
		inner join pg_attribute a
		on a.attrelid = c.oid
		inner join pg_type t
		on t.oid = a.atttypid
	WHERE c.relname = inv_table_name
		and a.attname in ('design_capacity','design_capacity_unit_numerator','design_capacity_unit_denominator')
		AND a.attnum > 0
	into has_design_capacity_columns;

	-- get strategy constraints
	SELECT csc.max_emis_reduction,
		csc.max_control_efficiency,
		csc.min_cost_per_ton,
		csc.min_ann_cost,
		csc.replacement_control_min_eff_diff
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

/*	EXECUTE '
		SELECT DISTINCT ON (poll) 1::integer as Found 
		FROM emissions.' || inv_table_name || ' as inv
		where ' || inv_filter || county_dataset_filter_sql || '
		and poll = ' || quote_literal((select name from emf.pollutants where id = target_pollutant_id)) || '
		limit 1'
	into has_target_pollutant;
	
	has_target_pollutant := coalesce(has_target_pollutant,0);

	IF has_target_pollutant = 0 THEN
		raise exception 'Target pollutant is not in the inventory';
		return;
	END IF;
*/
	raise notice '%', 'start call_batch_state ' || clock_timestamp();

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


	execute 'insert into emissions.' || detailed_result_table_name || ' 
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
		input_emis,
		output_emis,
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
		"comment"
		)
	select DISTINCT ON (inv.fips, inv.scc, ' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || 'er.pollutant_id) 
		' || detailed_result_dataset_id || '::integer,
		abbreviation,
		inv.poll,
		inv.scc,
		inv.fips,
		' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
		TO_CHAR(' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.operation_maintenance_cost, ''FM999999999999999990'')::double precision as operation_maintenance_cost,
		TO_CHAR(' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.annualized_capital_cost, ''FM999999999999999990'')::double precision as annualized_capital_cost,
		TO_CHAR(' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.capital_cost, ''FM999999999999999990'')::double precision as capital_cost,
		TO_CHAR(' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.annual_cost, ''FM999999999999999990'')::double precision as ann_cost,
		TO_CHAR(' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.computed_cost_per_ton, ''FM999999999999999990'')::double precision as computed_cost_per_ton,
		TO_CHAR(er.efficiency, ''FM990.099'')::double precision as efficiency,
		TO_CHAR(' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ', ''FM990.099'')::double precision as rule_pen,
		TO_CHAR(' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ', ''FM990.099'')::double precision as rule_eff,
		TO_CHAR(' || percent_reduction_sql || ', ''FM990.099'')::double precision as percent_reduction,
		inv.ceff,
		' || case when is_point_table = false then 'inv.rpen' else '100' end || ',
		inv.reff,
		' || annual_emis_sql || ' * (1 - ' || percent_reduction_sql || ' / 100) as final_emissions,
		' || annual_emis_sql || ' * ' || percent_reduction_sql || ' / 100 as emis_reduction,
		' || annual_emis_sql || ' as inv_emissions,
		' || annual_emis_sql || ' as input_emis,
		' || annual_emis_sql || ' * (1 - ' || percent_reduction_sql || ' / 100) as output_emis,
		1,
		substr(inv.fips, 1, 2),
		substr(inv.fips, 3, 3),
		' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ',
		' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ',
		inv.record_id::integer as source_id,
		' || input_dataset_id || '::integer,
		' || control_strategy_id || '::integer,
		er.control_measures_id,
		' || get_strategt_cost_sql || '.actual_equation_type as equation_type,
		''''
	FROM emissions.' || inv_table_name || ' inv

		inner join emf.pollutants p
		on p.name = inv.poll

		inner join (
			-- second pass, gets rid of ties for a paticular source
			select DISTINCT ON (fips, scc' || case when is_point_table = false then '' else ', plantid, pointid, stackid, segment' end || ') 
				abbreviation,
				scc,
				fips,
				' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '
				control_measures_id,
				equipment_life
			from (
				-- get best measures for sources target pollutants, there could be a tie for a paticular source.
				select DISTINCT ON (inv.fips, inv.scc' || case when is_point_table = false then '' else ', inv.plantid, inv.pointid, inv.stackid, inv.segment' end || ', er.control_measures_id) 
					m.abbreviation,
					inv.scc,
					inv.fips,
					' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment,' end || '
					er.control_measures_id,
					' || percent_reduction_sql || ' as percent_reduction,
					' || get_strategt_cost_inner_sql || '.computed_cost_per_ton as computed_cost_per_ton,
					m.equipment_life
--,cstp.precedence

				FROM emissions.' || inv_table_name || ' inv

					inner join emf.pollutants p
					on p.name = inv.poll

--					inner join (
--						select fips, scc, plantid, poll, sum(' || annual_emis_sql || ') as total_ann_emis
--						from emissions.' || inv_table_name || ' as inv
--						group by fips, scc, plantid, poll) source_total
--					on source_total.fips = inv.fips
--					and source_total.scc = inv.scc
--					and source_total.plantid = inv.plantid
--					and source_total.poll = inv.poll
					
					inner join emf.control_measure_sccs scc
					on scc.name = inv.scc
					
					' || case when measures_count > 0 then '
					inner join emf.control_strategy_measures csm
					on csm.control_measure_id = scc.control_measures_id
					and csm.control_strategy_id = ' || control_strategy_id || '
					' else '' end || '

			--this part will get applicable measure based on the target pollutant, use this measure for target and cobenefit pollutants...
					inner join emf.control_measures m
					on m.id = scc.control_measures_id

					inner join emf.control_measure_months ms
					on ms.control_measure_id = m.id
					and ms.month in (0' || case when dataset_month != 0 then ',' || dataset_month else '' end || ')

					left outer join emf.control_measure_equations eq
					on eq.control_measure_id = m.id
					and eq.pollutant_id = p.id

					left outer join emf.equation_types et
					on et.id = eq.equation_type_id

					left outer join reference.gdplev
					on gdplev.annual = eq.cost_year

					inner join emf.control_measure_efficiencyrecords er
					on er.control_measures_id = scc.control_measures_id
					-- pollutant filter
					and er.pollutant_id = p.id
					-- min and max emission filter
					and ' || annualized_emis_sql || ' between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
--					and source_total.total_ann_emis between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
					-- locale filter
					and (er.locale = inv.fips or er.locale = substr(inv.fips, 1, 2) or er.locale = '''')
					-- effecive date filter
					and ' || inventory_year || '::integer >= coalesce(date_part(''year'', er.effective_date), ' || inventory_year || '::integer)		

					' || case when measures_count = 0 and measure_classes_count > 0 then '
					inner join emf.control_strategy_classes csc
					on csc.control_measure_class_id = m.cm_class_id
					and csc.control_strategy_id = ' || control_strategy_id || '
					' else '' end || '

					-- target pollutant filter
--					inner join emf.control_strategy_target_pollutants cstp
--					on cstp.pollutant_id = p.id
--					and cstp.control_strategy_id = ' || control_strategy_id || '

				where 	' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
					and p.id = ' ||  target_pollutant_id || '
					and coalesce(100 * inv.ceff / 100 * coalesce(inv.reff / 100, 1.0)' || case when has_rpen_column then ' * coalesce(inv.rpen / 100, 1.0)' else '' end || ', 0) <> 100.0
					and er.efficiency - coalesce(inv.ceff, 0) >= ' || replacement_control_min_eff_diff_constraint || '
					-- measure region filter
					' || case when measure_with_region_count > 0 then '
					and 
					(
						csm.region_dataset_id is null 
						or
						(
							csm.region_dataset_id is not null 
							and exists (
								select 1
								from measures mr
									inner join measure_regions r
									on r.region_id = mr.region_id
									and r.region_version = mr.region_version
								where mr.control_measure_id = m.id
									and r.fips = inv.fips
							)
							and exists (
								select 1 
								from measures mr
								where mr.control_measure_id = m.id
							)
						)
					)					
					' else '' end || '
					-- constraints filter
					' || case when has_constraints then '
					and (
							' || percent_reduction_sql || ' >= ' || coalesce(min_control_efficiency_constraint, -100.0) || '
							' || coalesce(' and ' || percent_reduction_sql || ' / 100 * ' || annualized_emis_sql || ' >= ' || min_emis_reduction_constraint, '')  || '
							' || coalesce(' and coalesce(' || chained_gdp_adjustment_factor || '
								* ' || get_strategt_cost_inner_sql || '.computed_cost_per_ton, -1E+308) <= ' || max_cost_per_ton_constraint, '')  || '
							' || coalesce(' and coalesce(' || percent_reduction_sql || ' / 100 * ' || annualized_emis_sql || ' * ' || chained_gdp_adjustment_factor || '
								* ' || get_strategt_cost_inner_sql || '.computed_cost_per_ton, -1E+308) <= ' || max_ann_cost_constraint, '')  || '
					)' else '' end || '

				order by inv.fips, 
					inv.scc, ' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
					er.control_measures_id, 
					case when length(er.locale) = 5 then 0 when length(er.locale) = 2 then 1 else 2 end, 
					' || percent_reduction_sql || ' desc, 
					' || get_strategt_cost_inner_sql || '.computed_cost_per_ton
			) sm
			order by fips, 
				scc,' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '
--				precedence,
				percent_reduction desc, 
				computed_cost_per_ton
		) m
		on m.scc = inv.scc
		and m.fips = inv.fips
		' || case when is_point_table = false then '' else '
		and m.plantid = inv.plantid
		and m.pointid = inv.pointid
		and m.stackid = inv.stackid
		and m.segment = inv.segment' end || '

		' || case when measures_count > 0 then '
		inner join emf.control_strategy_measures csm
		on csm.control_measure_id = m.control_measures_id
		and csm.control_strategy_id = ' || control_strategy_id || '
		' else '' end || '

		left outer join emf.control_measure_equations eq
		on eq.control_measure_id = m.control_measures_id
		and eq.pollutant_id = p.id

		left outer join emf.equation_types et
		on et.id = eq.equation_type_id

		left outer join reference.gdplev
		on gdplev.annual = eq.cost_year

--					inner join (
--						select fips, scc, plantid, poll, sum(' || annual_emis_sql || ') as total_ann_emis
--						from emissions.' || inv_table_name || ' as inv
--						group by fips, scc, plantid, poll) source_total
--					on source_total.fips = inv.fips
--					and source_total.scc = inv.scc
--					and source_total.plantid = inv.plantid
--					and source_total.poll = inv.poll
					
--this part will get best eff rec...
		inner join emf.control_measure_efficiencyrecords er
		on er.control_measures_id = m.control_measures_id
		-- pollutant filter
		and er.pollutant_id = p.id
		-- min and max emission filter
		and ' || annualized_emis_sql || ' between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
--		and source_total.total_ann_emis between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
		-- locale filter
		and (er.locale = inv.fips or er.locale = substr(inv.fips, 1, 2) or er.locale = '''')
		-- effecive date filter
		and ' || inventory_year || '::integer >= coalesce(date_part(''year'', er.effective_date), ' || inventory_year || '::integer)

		-- target pollutant filter
--		left outer join emf.control_strategy_target_pollutants cstp
--		on cstp.pollutant_id = p.id
--		and cstp.control_strategy_id = ' || control_strategy_id || '

	where 	' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
		and coalesce(100 * inv.ceff / 100 * coalesce(inv.reff / 100, 1.0) * ' || case when has_rpen_column then 'coalesce(inv.rpen / 100, 1.0)' else '1.0' end || ', 0) <> 100.0
		and (	p.id <> ' ||  target_pollutant_id || '
			or 
			(
				p.id = ' ||  target_pollutant_id || '
				and er.efficiency - coalesce(inv.ceff, 0) >= ' || replacement_control_min_eff_diff_constraint || '
			)
		)
		-- constraint filter, BUT ONLY for target pollutants
		' || case when has_constraints then '
		and (	p.id <> ' ||  target_pollutant_id || ' --cstp.pollutant_id is null 
			or 
			(
				p.id = ' ||  target_pollutant_id || '-- cstp.pollutant_id is not null  
				and (
					' || percent_reduction_sql || ' >= ' || coalesce(min_control_efficiency_constraint, -100.0) || '
					' || coalesce(' and ' || percent_reduction_sql || ' / 100 * ' || annualized_emis_sql || ' >= ' || min_emis_reduction_constraint, '')  || '
					' || coalesce(' and coalesce(' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.computed_cost_per_ton, -1E+308) <= ' || max_cost_per_ton_constraint, '')  || '
					' || coalesce(' and coalesce(' || percent_reduction_sql || ' / 100 * ' || annualized_emis_sql || ' * ' || chained_gdp_adjustment_factor || ' * ' || get_strategt_cost_sql || '.computed_cost_per_ton, -1E+308) <= ' || max_ann_cost_constraint, '')  || '
				)
			)
		)' else '' end || '

	order by inv.fips, 
		inv.scc, 
		' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
		er.pollutant_id, 
		case when length(er.locale) = 5 then 0 when length(er.locale) = 2 then 1 else 2 end, 
		' || percent_reduction_sql || ' desc, 
		' || get_strategt_cost_sql || '.computed_cost_per_ton';

	return;
END;
$$ LANGUAGE plpgsql;