--delete from emissions.csdr_test5;
--vacuum emissions.csdr_test5;
--ANALYZE verbose emissions.csdr_test5;

--select * from emissions.csdr_test5 order by source_id, record_id limit 1000;

CREATE OR REPLACE FUNCTION public.run_apply_measures_in_series_strategy_batch_by_state(control_strategy_id integer, 
	input_dataset_id integer, 
	inv_table_name varchar(63), 
	inv_filter varchar, 
	detailed_result_dataset_id integer, 
	detailed_result_table_name varchar(63), 
	strfipsst varchar(2), 
	measures_count integer, 
	measure_classes_count integer, 
	target_pollutant_id integer,
	inventory_year integer, 
	county_dataset_filter_sql text,
	is_point_table boolean,
	has_sic_column boolean, 
	has_naics_column boolean) RETURNS integer AS $$
DECLARE
	dataset_month smallint := 0;
	no_days_in_month smallint := 31;
	run_status character varying;
BEGIN
	-- see if the strategy has been canceled, is so get out of procedure...
	select control_strategies.run_status 
	from emf.control_strategies
	where id = control_strategy_id
	into run_status;

	IF run_status = 'Cancelled' THEN
		RAISE EXCEPTION 'Strategy was cancelled';
		return 0;
	END IF;

	SET enable_seqscan TO 'off';
	SET work_mem TO '512MB';
--	SET enable_nestloop TO 'off';

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

	raise notice '%', 'start ' || strfipsst || ' ' || clock_timestamp();

--	EXECUTE 
	EXECUTE 'insert into emissions.' || detailed_result_table_name || ' 
		(
		dataset_id,
		cm_abbrev,
		poll,
		scc,
		fips,
		' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '
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
		fipsst,
		fipscty,
		sic,
		naics,
		source_id,
		input_ds_id,
		cs_id,
		cm_id
		)
	select 	' || detailed_result_dataset_id || '::integer,
		abbreviation,
		poll,
		scc,
		fips,
		' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || '
		ann_cost,
		ref_yr_cost_per_ton,
		efficiency,
		rule_pen,
		rule_eff,
		percent_reduction,
		ceff,
		rpen,
		reff,
		final_emissions,
		emis_reduction,
		inv_emissions,
		fipsst,
		fipscty,
		sic,
		naics,
		source_id,
		' || input_dataset_id || '::integer,
		' || control_strategy_id || '::integer,
		cm_id
	from (
		select DISTINCT ON (inv.scc, inv.fips, ' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || 'er.pollutant_id, er.control_measures_id) 
			m.abbreviation,
			inv.poll,
			inv.scc,
			inv.fips,
			' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
			ref_yr_cost_per_ton * coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis) * efficiency * ' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' * ' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' / 100 / 100 / 100 as ann_cost,
			ref_yr_cost_per_ton,
			efficiency,
			' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' as rule_pen,
			' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' as rule_eff,
			efficiency * ' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' * ' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' / 100 / 100 as percent_reduction,
			inv.ceff,
			' || case when is_point_table = false then 'inv.rpen' else '100' end || ' as rpen,
			inv.reff,
			' || case when dataset_month != 0 then 'coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis)' else 'inv.ann_emis' end || ' * (1 - efficiency * ' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' * ' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' / 100 / 100 / 100) as final_emissions,
			' || case when dataset_month != 0 then 'coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis)' else 'inv.ann_emis' end || ' * efficiency * ' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' * ' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' / 100 / 100 / 100 as emis_reduction,
			' || case when dataset_month != 0 then 'coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis)' else 'inv.ann_emis' end || ' as inv_emissions,
			substr(inv.fips, 1, 2) as fipsst,
			substr(inv.fips, 3, 3) as fipscty,
			' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ' as sic,
			' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ' as naics,
			inv.record_id::integer as source_id,
			er.control_measures_id as cm_id,
			' || case when measures_count > 0 then 'csm.apply_order ' else '1.0' end || ' as apply_order
		FROM emissions.' || inv_table_name || ' inv

			inner join emf.pollutants p
			on p.name = inv.poll

			inner join emf.control_measure_sccs scc
			on scc.name = inv.scc

			' || case when measures_count > 0 then '
			inner join emf.control_strategy_measures csm
			on csm.control_measure_id = scc.control_measures_id
			and csm.control_strategy_id = ' || control_strategy_id || '
			' else '' end || '

			inner join emf.control_measures m
			on m.id = scc.control_measures_id

			inner join emf.control_measure_months ms
			on ms.control_measure_id = m.id
			and ms.month in (0' || case when dataset_month != 0 then ',' || dataset_month else '' end || ')

			inner join emf.control_measure_efficiencyrecords er
			on er.control_measures_id = scc.control_measures_id
			-- pollutant filter
			and er.pollutant_id = p.id
			-- min and max emission filter
			and ' || case when dataset_month != 0 then 'coalesce(inv.avd_emis * 365, inv.ann_emis)' else 'inv.ann_emis' end || ' / (1 - coalesce(inv.ceff / 100 * inv.reff / 100 * ' || case when is_point_table = false then 'inv.rpen' else '100' end || ' / 100, 0)) between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
			-- locale filter
			and (er.locale = inv.fips or er.locale = substr(inv.fips, 1, 2) or er.locale = '''')
			-- effecive date filter
			and ' || inventory_year || '::integer >= coalesce(date_part(''year'', er.effective_date), ' || inventory_year || '::integer)

			' || case when measures_count = 0 and measure_classes_count > 0 then '
			inner join emf.control_strategy_classes csc
			on csc.control_measure_class_id = m.cm_class_id
			and csc.control_strategy_id = ' || control_strategy_id || '
			' else '' end || '

		where 	' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
			and inv.fips like ' ||  quote_literal(strfipsst || '%') || '

		order by inv.scc, inv.fips, ' || case when is_point_table = false then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || 'er.pollutant_id, 
			er.control_measures_id, case when length(locale) = 5 then 0 when length(locale) = 2 then 1 else 2 end, ref_yr_cost_per_ton, efficiency * ' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' * ' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' desc
		) as tbl
	order by scc, fips, ' || case when is_point_table = false then '' else 'plantid, pointid, stackid, segment, ' end || 'poll, apply_order, coalesce(ref_yr_cost_per_ton, 0), percent_reduction desc';
	
	raise notice '%', 'end ' || strfipsst || ' ' || clock_timestamp();

	RETURN 1;
END;
$$ LANGUAGE plpgsql;



CREATE OR REPLACE FUNCTION public.run_apply_measures_in_series_strategy(control_strategy_id integer, 
	input_dataset_id integer, 
	input_dataset_version integer, 
	strategy_result_id int) RETURNS integer AS $$
DECLARE
	inv_table_name varchar(63) := '';
	inv_filter varchar := '';
	inv_fips_filter text := '';
	detailed_result_dataset_id integer := null;
	detailed_result_table_name varchar(63) := '';
	county_dataset_id integer := null;
	county_dataset_version integer := null;
	region RECORD;
	target_pollutant_id integer := 0;
	measures_count integer := 0;
	measure_classes_count integer := 0;
	county_dataset_filter_sql text := '';
	cost_year integer := null;
	inventory_year integer := null;
	is_point_table boolean := false;
	has_sic_column boolean := false; 
	has_naics_column boolean := false;
BEGIN
	SET work_mem TO '512MB';
	SET enable_seqscan TO 'off';

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
	SELECT count(id)
	FROM emf.control_strategy_measures 
	where control_strategy_measures.control_strategy_id = control_strategy_id 
	INTO measures_count;

	-- see if measure classes were specified
	IF measures_count = 0 THEN
		SELECT count(1)
		FROM emf.control_strategy_classes 
		where control_strategy_classes.control_strategy_id = control_strategy_id
		INTO measure_classes_count;
	END IF;

	-- get target pollutant, inv filter, and county dataset info if specified
	SELECT cs.pollutant_id,
		case when length(trim(cs.filter)) > 0 then '(' || cs.filter || ')' else null end,
		cs.cost_year,
		cs.analysis_year,
		cs.county_dataset_id,
		cs.county_dataset_version
	FROM emf.control_strategies cs
	where cs.id = control_strategy_id
	INTO target_pollutant_id,
		inv_filter,
		cost_year,
		inventory_year,
		county_dataset_id,
		county_dataset_version;

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
	SELECT true
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
	SELECT true
	FROM pg_class c
		inner join pg_attribute a
		on a.attrelid = c.oid
		inner join pg_type t
		on t.oid = a.atttypid
	WHERE c.relname = inv_table_name
		and a.attname = 'naics'
		AND a.attnum > 0
	into has_naics_column;

	-- see if their was a county dataset specified for the strategy, is so then build a sql where clause filter for later use
	IF county_dataset_id is not null THEN
		county_dataset_filter_sql := ' and fips in (SELECT fips
			FROM emissions.' || (SELECT table_name FROM emf.internal_sources where dataset_id = county_dataset_id) || '
			where ' || public.build_version_where_filter(county_dataset_id, county_dataset_version) || ')';
	END IF;
	-- build version info into where clause filter
	inv_filter := '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version) || ')' || coalesce(' and ' || inv_filter, '');

	raise notice '%', 'start call_batch_state ' || clock_timestamp();

        BEGIN
		execute 'select public.run_apply_measures_in_series_strategy_batch_by_state(' || control_strategy_id || ', 
			' || input_dataset_id  || ', 
			' || quote_literal(inv_table_name) || ', 
			' || quote_literal(inv_filter) || ', 
			' || detailed_result_dataset_id || ', 
			' || quote_literal(detailed_result_table_name) || ', 
			fipsst, 
			' || measures_count || ', 
			' || measure_classes_count || ', 
			' || target_pollutant_id || ', 
			' || inventory_year || ', 
			' || quote_literal(coalesce(county_dataset_filter_sql, '')) || ',
			' || case when is_point_table then 'true' else 'false' end || '::boolean,
			' || case when has_sic_column then 'true' else 'false' end || '::boolean, 
			' || case when has_naics_column then 'true' else 'false' end || '::boolean
			)
		from (
			SELECT DISTINCT ON (substring(fips, 1, 2)) substring(fips, 1, 2) as fipsst
			FROM emissions.' || inv_table_name || ' as inv
			where ' || inv_filter || county_dataset_filter_sql || '
			order by substring(fips, 1, 2) 
	--		limit 1
		) fips';
            RETURN 1;
        EXCEPTION when raise_exception then
		--update the strategy result to cancelled
		update emf.strategy_results
		set run_status = 'Cancelled'
		where id = strategy_result_id;
		RAISE EXCEPTION 'Strategy was cancelled';
        END;

	raise notice '%', 'end call_batch_state ' || clock_timestamp();

	RETURN 1;
END;
$$ LANGUAGE plpgsql;





CREATE OR REPLACE FUNCTION public.run_apply_measures_in_series_strategy_finalize(control_strategy_id integer, input_dataset_id integer, 
	input_dataset_version integer, strategy_result_id int) RETURNS integer AS $$
DECLARE
	inv_table_name varchar(63) := '';
	inv_fips_filter text := '';
	detailed_result_dataset_id integer := null;
	detailed_result_table_name varchar(63) := '';
	region RECORD;
	target_pollutant_id integer := 0;
	target_pollutant varchar(255) := '';
	measure_with_region_count integer := 0;
	min_emis_reduction_constraint real := null;
	min_control_efficiency_constraint real := null;
	max_cost_per_ton_constraint real := null;
	max_ann_cost_constraint real := null;
	has_constraints boolean := null;
	cost_year integer := null;
	ref_cost_year integer := 2006;
	cost_year_chained_gdp double precision := null;
	ref_cost_year_chained_gdp double precision := null;
	is_point_table boolean := false;
	gimme_count integer := 0;
BEGIN
	SET work_mem TO '512MB';
	SET enable_seqscan TO 'off';

	-- get the input dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = input_dataset_id
	into inv_table_name;

	-- get the detailed result dataset info
	select lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
	where sr.id = strategy_result_id
	into detailed_result_table_name;

	-- see if control strategy has only certain measures specified
	SELECT count(1)
	FROM emf.control_strategy_measures 
	where control_strategy_measures.control_strategy_id = control_strategy_id 
		and control_strategy_measures.region_dataset_id is not null
	INTO measure_with_region_count;

	-- get target pollutant, inv filter, and county dataset info if specified
	SELECT cs.pollutant_id,
		cs.cost_year
	FROM emf.control_strategies cs
	where cs.id = control_strategy_id
	INTO target_pollutant_id,
		cost_year;
	
	select p.name
	FROM emf.pollutants p
	where p.id = target_pollutant_id
	INTO target_pollutant;

	-- get gdp chained values
	SELECT cast(chained_gdp as double precision)
	FROM reference.gdplev
	where annual = cost_year
	INTO cost_year_chained_gdp;
	SELECT cast(chained_gdp as double precision)
	FROM reference.gdplev
	where annual = ref_cost_year
	INTO ref_cost_year_chained_gdp;

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

	-- see if there are point specific columns to be indexed
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

	-- if strategy have measures, then store these in a temp table for later use...
	IF measure_with_region_count > 0 THEN
		EXECUTE '
			CREATE TEMP TABLE measures (control_measure_id integer NOT NULL, region_id integer NOT NULL, region_version integer NOT NULL) ON COMMIT DROP;
			CREATE TEMP TABLE measure_regions (region_id integer NOT NULL, region_version integer NOT NULL, fips character varying(6) NOT NULL) ON COMMIT DROP;

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

	raise notice '%', 'start call_batch_state ' || clock_timestamp();

	-- get rid of sources that use measures not in there county, this is post step becuase using a join during the measure selection process seem to be slower.
	IF measure_with_region_count > 0 THEN
--		execute 'raise notice ''%'', ''get rid of sources that use measures not in there county - before count '' || (select count(1) from emissions.' || detailed_result_table_name || ') || '' '' || clock_timestamp();';
--		raise notice '%', 'get rid of sources that use measures not in there county - before count ' || (select count(1) from emissions.csdr_test5) || ' ' || clock_timestamp();


		EXECUTE	'delete from only emissions.' || detailed_result_table_name || ' as inv2
			where	not exists (
					select 1
					from measures mr
						inner join measure_regions r
						on r.region_id = mr.region_id
						and r.region_version = mr.region_version
					where mr.control_measure_id = inv2.cm_id
						and r.fips = inv2.fips
					)
				and exists (
					select 1 
					from measures m
					where m.control_measure_id = inv2.cm_id
					)';

/*
--different approaches that are slower...
		EXECUTE	'delete from only emissions.' || detailed_result_table_name || ' as inv
			where not exists (
			select 1
			from emissions.' || detailed_result_table_name || ' inv2
				inner join measures mr
				on mr.control_measure_id = inv2.cm_id
				inner join measure_regions r
				on r.region_id = mr.region_id
				and r.region_version = mr.region_version
				and r.fips = inv2.fips
			where 	inv2.record_id = inv.record_id
			)
			and inv.cm_id in (select control_measure_id from measures)';

		EXECUTE	'delete from only emissions.' || detailed_result_table_name || ' as inv
			where not exists (
			select 1
			from emissions.' || detailed_result_table_name || ' inv2
				inner join measure_region mr
				on mr.control_measure_id = inv2.cm_id
				and mr.fips = inv2.fips
			where 	inv2.record_id = inv.record_id
			)
			and inv.cm_id in (select distinct control_measure_id from measure_region)';


		EXECUTE	'delete from only emissions.' || detailed_result_table_name || '
				where record_id in (
					select record_id
					from emissions.' || detailed_result_table_name || ' as inv
						inner join emf.control_strategy_measures csm
						on csm.control_measure_id = inv.cm_id
						and csm.region_dataset_id is not null
						left outer join measure_region mr
						on mr.control_measure_id = inv.cm_id
						and mr.fips = inv.fips
					where mr.fips is null
						and csm.control_strategy_id = ' || control_strategy_id || ')
		';

		EXECUTE	'delete from only emissions.' || detailed_result_table_name || ' as inv2
			where	not exists (
					select 1
					from measures mr
						inner join measure_regions r
						on r.region_id = mr.region_id
						and r.region_version = mr.region_version
					where mr.control_measure_id = inv2.cm_id
						and r.fips = inv2.fips
					)
				and exists (
					select 1 
					from measures m
					where m.control_measure_id = inv2.cm_id
					)';
*/

--		raise notice '%', 'after count ' || (select count(1) from emissions.csdr_test5) || ' ' || clock_timestamp();
	END IF;

--	SET enable_seqscan TO 'on';
--	execute 'raise notice ''%'', ''readjust detailed result - count '' || (select count(1) from emissions.' || detailed_result_table_name || ') || '' '' || clock_timestamp();';

	-- update the detailed result
/*raise notice '%', 
	'update emissions.' || detailed_result_table_name || ' as inv
	set 	inv_emissions = case when (select min(inv2.record_id) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id) = inv.record_id then inv.inv_emissions else null end,
		final_emissions = case when (select max(inv2.record_id) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id) = inv.record_id then inv.inv_emissions * (select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id) else null end,
		emis_reduction = inv.inv_emissions * inv.percent_reduction / 100 * coalesce((select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id < inv.record_id), 1),
		annual_cost = inv.ann_cost_per_ton * inv.inv_emissions * inv.percent_reduction / 100 * coalesce((select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id < inv.record_id), 1),
		apply_order = (select count(1) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id <= inv.record_id)';
*/
	EXECUTE	'update emissions.' || detailed_result_table_name || ' as inv
	set 	emis_reduction = inv.inv_emissions * inv.percent_reduction / 100 * coalesce((select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id < inv.record_id), 1),
		annual_cost = ' || cost_year_chained_gdp || ' / ' || ref_cost_year_chained_gdp || ' * inv.ann_cost_per_ton * inv.inv_emissions * inv.percent_reduction / 100 * coalesce((select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id < inv.record_id), 1),
		apply_order = (select count(1) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id <= inv.record_id),
		input_emis = inv.inv_emissions * coalesce((select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id < inv.record_id), 1),
		output_emis = inv.inv_emissions * (1 - inv.percent_reduction / 100) * coalesce((select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id < inv.record_id), 1),
		final_emissions = null,
		ann_cost_per_ton = ' || cost_year_chained_gdp || ' / ' || ref_cost_year_chained_gdp || ' * ann_cost_per_ton';

	-- make sure we meet the constraints, if not get rid of the applicable measures...
	IF has_constraints THEN
--		raise notice '%', 'get rid of sources that use measures not in there county - before count ' || (select count(1) from emissions.csdr_test5) || ' ' || clock_timestamp();
/*		execute 'create table emissions.noconstraints as 
			select scc, fips, cm_id, inv2.percent_reduction, inv2.poll, inv2.emis_reduction, inv2.ann_cost_per_ton, inv2.annual_cost
			from emissions.' || detailed_result_table_name || ' as inv2
			where inv2.poll = ' || quote_literal(target_pollutant) || '';
		execute 'create table emissions.constraints as 
			select scc, fips, cm_id, inv2.percent_reduction, inv2.poll, inv2.emis_reduction, inv2.ann_cost_per_ton, inv2.annual_cost
			from emissions.' || detailed_result_table_name || ' as inv2
			where inv2.poll = ' || quote_literal(target_pollutant) || '
				and (
					inv2.percent_reduction < ' || coalesce(min_control_efficiency_constraint, -100.0) || '
					' || coalesce(' or inv2.emis_reduction < ' || min_emis_reduction_constraint, '')  || '
					' || coalesce(' or inv2.ann_cost_per_ton > ' || max_cost_per_ton_constraint, '')  || '
					' || coalesce(' or inv2.annual_cost > ' || max_ann_cost_constraint, '')  || '
				)';
*/
		execute 'delete from emissions.' || detailed_result_table_name || ' as inv
			using emissions.' || detailed_result_table_name || ' as inv2
			where not exists (select 1 
				from emissions.' || detailed_result_table_name || ' as inv2
				where inv2.scc = inv.scc
				and inv2.fips = inv.fips
				' || case when is_point_table = false then '' else '
				and inv2.plantid = inv.plantid
				and inv2.pointid = inv.pointid
				and inv2.stackid = inv.stackid
				and inv2.segment = inv.segment' end || '
				and inv2.cm_id = inv.cm_id
				and inv2.poll = ' || quote_literal(target_pollutant) || '
				and (
					inv2.percent_reduction >= ' || coalesce(min_control_efficiency_constraint, -100.0) || '
					' || coalesce(' and inv2.emis_reduction >= ' || min_emis_reduction_constraint, '')  || '
					' || coalesce(' and coalesce(inv2.ann_cost_per_ton, -1E+308) <= ' || max_cost_per_ton_constraint, '')  || '
					' || coalesce(' and coalesce(inv2.annual_cost, -1E+308) <= ' || max_ann_cost_constraint, '')  || '
				))
				and exists (select 1 
					from emissions.' || detailed_result_table_name || ' as inv3
					where inv3.fips = inv.fips
						and inv3.scc = inv.scc
						' || case when is_point_table = false then '' else '
						and inv3.plantid = inv.plantid
						and inv3.pointid = inv.pointid
						and inv3.stackid = inv.stackid
						and inv3.segment = inv.segment' end || '
						and inv3.poll = ' || quote_literal(target_pollutant) || '
					)
				';
		-- update the apply order again, there are bound to be gaps...
		EXECUTE	'update emissions.' || detailed_result_table_name || ' as inv
		set 	apply_order = (select count(1) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id <= inv.record_id)';

	END IF;

/*	'inv_emissions = case when (select min(inv2.record_id) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id) = inv.record_id then inv.inv_emissions else null end,
		final_emissions = case when (select max(inv2.record_id) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id) = inv.record_id then inv.inv_emissions * (select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id) else null end,
		emis_reduction = inv.inv_emissions * inv.percent_reduction / 100 * coalesce((select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id < inv.record_id), 1),
		annual_cost = inv.ann_cost_per_ton * inv.inv_emissions * inv.percent_reduction / 100 * coalesce((select public.times(1 - inv2.percent_reduction / 100) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id < inv.record_id), 1),
		apply_order = (select count(1) from emissions.' || detailed_result_table_name || ' as inv2 where inv2.source_id = inv.source_id and inv2.record_id <= inv.record_id)';
*/
	EXECUTE	'update emissions.' || detailed_result_table_name || ' as inv
	set 	inv_emissions = null 
	where apply_order > 1';

	EXECUTE	'update emissions.' || detailed_result_table_name || ' as inv
	set 	final_emissions = (select min(output_emis) from emissions.' || detailed_result_table_name || ' where source_id = inv.source_id)
	where apply_order = 1 ';

	raise notice '%', 'end call_batch_state ' || clock_timestamp();

	RETURN 1;
END;
$$ LANGUAGE plpgsql;

--95.3 mins...

--select public.run_apply_measures_in_series_strategy(25672, 284);

-- delete from emissions.CSDR_1082_V0_20080105060526onroad_2020cc_jan_06jun2007_v0
-- vacuum verbose emissions.CSDR_1082_V0_20080105060526onroad_2020cc_jan_06jun2007_v0
-- select count(*) from emissions.CSDR_1082_V0_20080105060526onroad_2020cc_jan_06jun2007_v0
-- select distinct cm_abbrev from emissions.CSDR_1082_V0_20080105060526onroad_2020cc_jan_06jun2007_v0 order by cm_abbrev
--select * from emissions.DS_ptinv_ny_36_orl_txt_314836372 --CSDR_1065_V0_20080110140407ptinv_ny_36_orl_txt
--SELECT public.run_apply_measures_in_series_strategy(25694, 300);


--SELECT public.create_strategy_detailed_result_table_indexes('CSDR_842_V0_20071229041920onal_Controls_w_NLEV_Crxns_OnRoad_txt');
--SELECT public.run_apply_measures_in_series_strategy(25694, 304);
--SELECT public.create_strategy_detailed_result_table_indexes('CSDR_842_V0_20071231034701onal_Controls_w_NLEV_Crxns_OnRoad_txt');SELECT public.run_apply_measures_in_series_strategy(25694, 316)
-- SELECT public.create_strategy_detailed_result_table_indexes('CSDR_1082_V0_20080105060526onroad_2020cc_jan_06jun2007_v0');
--SELECT public.run_apply_measures_in_series_strategy(11, 1082, 0, 51);
--SELECT public.run_apply_measures_in_series_strategy(50, 1065, 0, 367)
/*
select *
from emissions.CSDR_1315_V0_20080115222850onroad_2020cc_nov_06jun2007_v0 inv
where not exists (
select 1
from emissions.CSDR_1315_V0_20080115222850onroad_2020cc_nov_06jun2007_v0 inv2
	inner join measure_region mr
	on mr.control_measure_id = inv2.cm_id
	and mr.fips = inv2.fips
where 	inv2.record_id = inv.record_id
)
and inv.cm_id in (select distinct control_measure_id from measure_region)

--	left outer join measure_region mr
--	on mr.control_measure_id = inv.cm_id
--	and mr.fips = inv.fips
where inv.cm_id in (select distinct control_measure_id from measure_region)
--and mr.fips is null

select *
from emissions.CSDR_1315_V0_20080115222850onroad_2020cc_nov_06jun2007_v0 inv
	left outer join measure_region mr
	on mr.control_measure_id = inv.cm_id
	and mr.fips = inv.fips
where inv.cm_id in (select distinct control_measure_id from measure_region)
and mr.control_measure_id is null and mr.fips is null


SELECT public.run_apply_measures_in_series_strategy(67, 1315, 0, 419);
--SELECT public.create_strategy_detailed_result_table_indexes('CSDR_1315_V0_20080115232119onroad_2020cc_nov_06jun2007_v0');analyze emissions.CSDR_1315_V0_20080115232119onroad_2020cc_nov_06jun2007_v0;
SELECT public.run_apply_measures_in_series_strategy_finalize(67, 1315, 0, 419);
--delete from emissions.CSDR_1315_V0_20080115232119onroad_2020cc_nov_06jun2007_v0

*/