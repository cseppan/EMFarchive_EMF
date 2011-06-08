drop FUNCTION public.populate_least_cost_strategy_worksheet(integer, 
	integer, 
	integer);

CREATE OR REPLACE FUNCTION public.populate_least_cost_strategy_worksheet(int_control_strategy_id integer, 
	input_dataset_id integer, 
	input_dataset_version integer) RETURNS void AS $$
DECLARE
	strategy_name varchar(255) := '';
	inv_table_name varchar(64) := '';
	inv_filter text := '';
	inv_fips_filter text := '';
	worksheet_dataset_id integer := null;
	worksheet_table_name varchar(64) := '';
	county_dataset_id integer := null;
	county_dataset_version integer := null;
	region RECORD;
	target_pollutant_id integer := 0;
	target_pollutant varchar;
	measures_count integer := 0;
	measure_with_region_count integer := 0;
	measure_classes_count integer := 0;
	county_dataset_filter_sql text := '';
	cost_year integer := null;
	inventory_year integer := null;
	is_point_table boolean := false;
	use_cost_equations boolean := false;
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
	has_rpen_column boolean := false;
	has_merged_columns boolean := false;
	has_latlong_columns boolean := false;
	has_plant_column boolean := false;
	gimme_count integer := 0;
	min_emis_reduction_constraint real := null;
	min_control_efficiency_constraint real := null;
	max_cost_per_ton_constraint real := null;
	max_ann_cost_constraint real := null;
	replacement_control_min_eff_diff_constraint double precision := null;
	has_constraints boolean := null;
	str varchar;
	marginal double precision;
	emis_reduction double precision;
	record_id double precision;
	apply_order integer;
	domain_wide_emis_reduction double precision;
	domain_wide_pct_reduction double precision;
	domain_wide_pct_reduction_increment double precision;
	domain_wide_pct_reduction_start double precision;
	domain_wide_pct_reduction_end double precision;
	counter integer := 0;
	record_count integer := 0;
	deleted_record_count integer := 0;
	increasing_trend boolean := false;
	prev_apply_order integer;
	get_strategt_cost_sql character varying;
	get_strategt_cost_inner_sql character varying;
	annualized_uncontrolled_emis_sql character varying;
	uncontrolled_emis_sql character varying;
	emis_sql character varying;
	percent_reduction_sql character varying;
	sector character varying := '';
	strategy_type character varying(255);
	include_unspecified_costs boolean := true; 
	has_cpri_column boolean := false; 
	has_primary_device_type_code_column boolean := false; 
	remaining_emis_sql character varying;
	creator_user_id integer := 0;
	is_cost_su boolean := false; 

	get_strategty_ceff_equation_sql character varying;
	annual_cost_expression text;
	capital_cost_expression text;
	operation_maintenance_cost_expression text;
	fixed_operation_maintenance_cost_expression text;
	variable_operation_maintenance_cost_expression text;
	annualized_capital_cost_expression text;
	computed_cost_per_ton_expression text;
	actual_equation_type_expression text;
BEGIN
--	SET work_mem TO '256MB';
--	SET enable_seqscan TO 'off';
--	SET enable_nestloop TO 'on';

	raise notice '%', 'start public.populate_least_cost_strategy_worksheet ' || clock_timestamp();

	-- get the input dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = input_dataset_id
	into inv_table_name;

	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
		inner join emf.strategy_result_types srt
		on srt.id = sr.strategy_result_type_id
	where sr.control_strategy_id = int_control_strategy_id 
		and srt.name = 'Least Cost Control Measure Worksheet'
	into worksheet_dataset_id,
		worksheet_table_name;

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
	SELECT cs.name,
		cs.pollutant_id,
		case when length(trim(cs.filter)) > 0 then '(' || public.alias_filter(cs.filter, inv_table_name, 'inv') || ')' else null end,
		cs.cost_year,
		cs.analysis_year,
		cs.county_dataset_id,
		cs.county_dataset_version,
		cs.use_cost_equations,
		cs.discount_rate / 100,
		st."name",
		coalesce(cs.include_unspecified_costs,true),
		cs.creator_id
	FROM emf.control_strategies cs
		inner join emf.strategy_types st
		on st.id = cs.strategy_type_id
	where cs.id = int_control_strategy_id
	INTO strategy_name,
		target_pollutant_id,
		inv_filter,
		cost_year,
		inventory_year,
		county_dataset_id,
		county_dataset_version,
		use_cost_equations,
		discount_rate,
		strategy_type,
		include_unspecified_costs,
		creator_user_id;

	-- get target pollutant name
	select name
	from emf.pollutants
	where id = target_pollutant_id
	into target_pollutant;


	-- see if there are point specific columns in the inventory
	is_point_table := public.check_table_for_columns(inv_table_name, 'plantid,pointid,stackid,segment', ',');

	-- see if there is a sic column in the inventory
	has_sic_column := public.check_table_for_columns(inv_table_name, 'sic', ',');

	-- see if there is a naics column in the inventory
	has_naics_column := public.check_table_for_columns(inv_table_name, 'naics', ',');

	-- see if there is a rpen column in the inventory
	has_rpen_column := public.check_table_for_columns(inv_table_name, 'rpen', ',');

	-- see if there is design capacity columns in the inventory
	has_design_capacity_columns := public.check_table_for_columns(inv_table_name, 'design_capacity,design_capacity_unit_numerator,design_capacity_unit_denominator', ',');

	-- see if there is lat & long columns in the inventory
	has_latlong_columns := public.check_table_for_columns(inv_table_name, 'xloc,yloc', ',');

	-- see if there is lat & long columns in the inventory
	has_plant_column := public.check_table_for_columns(inv_table_name, 'plant', ',');

	-- see if this a merged orl inventory
	has_merged_columns := public.check_table_for_columns(inv_table_name, 'original_dataset_id,sector', ',');

	-- see if there is plant column in the inventory
	has_cpri_column := public.check_table_for_columns(inv_table_name, 'cpri', ',');

	-- see if there is primary_device_type_code column in the inventory
	has_primary_device_type_code_column := public.check_table_for_columns(inv_table_name, 'primary_device_type_code', ',');

	-- get sector of the inventory if this is not a merged orl inventory
	--get the inventory sector(s)
	select public.concatenate_with_ampersand(distinct name)
	from emf.sectors s
		inner join emf.datasets_sectors ds
		on ds.sector_id = s.id
	where ds.dataset_id = input_dataset_id
	into sector;

	-- get strategy constraints
	SELECT csc.max_emis_reduction,
		csc.max_control_efficiency,
		csc.min_cost_per_ton,
		csc.min_ann_cost,
		csc.domain_wide_emis_reduction,
		csc.domain_wide_pct_reduction,
		csc.replacement_control_min_eff_diff/*,
		csc.domain_wide_pct_reduction_increment,
		csc.domain_wide_pct_reduction_start,
		csc.domain_wide_pct_reduction_end*/
	FROM emf.control_strategy_constraints csc
	where csc.control_strategy_id = int_control_strategy_id
	INTO min_emis_reduction_constraint,
		min_control_efficiency_constraint,
		max_cost_per_ton_constraint,
		max_ann_cost_constraint,
		domain_wide_emis_reduction,
		domain_wide_pct_reduction,
		replacement_control_min_eff_diff_constraint/*,
		domain_wide_pct_reduction_increment,
		domain_wide_pct_reduction_start,
		domain_wide_pct_reduction_end*/;

/*	IF coalesce(domain_wide_pct_reduction_increment, 0.0) = 0.0 THEN
		RAISE EXCEPTION 'Missing domain wide percentage reduction increment.';
		return;
	END IF;
*/
	select case when min_emis_reduction_constraint is not null 
		or min_control_efficiency_constraint is not null 
		or max_cost_per_ton_constraint is not null 
		or max_ann_cost_constraint is not null then true else false end
	into has_constraints;

	-- get month of the dataset, 0 (Zero) indicates an annual inventory
	select public.get_dataset_month(input_dataset_id)
	into dataset_month;

	select public.get_days_in_month(dataset_month::smallint, inventory_year::smallint)
	into no_days_in_month;

	-- if strategy has specific measures assigned, then store these in a temp table for later use...
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
			where m.control_strategy_id = ' || int_control_strategy_id || '
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
	inv_filter := '(' || public.build_version_where_filter(input_dataset_id, input_dataset_version, 'inv') || ')' || coalesce(' and ' || inv_filter, '');

	--do this only for the Least Cost strategy type...
	IF strategy_type = 'Least Cost' THEN
		-- get strategy constraints
		SELECT csc.domain_wide_emis_reduction,
			csc.domain_wide_pct_reduction
		FROM emf.control_strategy_constraints csc
		where csc.control_strategy_id = int_control_strategy_id
		INTO domain_wide_emis_reduction,
		domain_wide_pct_reduction;

		IF coalesce(domain_wide_emis_reduction, domain_wide_pct_reduction, 0.0) = 0.0 THEN
			RAISE EXCEPTION 'Missing domain wide emission (or percentage) reduction.';
			return;
		END IF;
		-- figure out domain_wide_emis_reduction, if pct red was passed in
		IF coalesce(domain_wide_pct_reduction, 0.0) <> 0.0 THEN
			execute 'select ' || domain_wide_pct_reduction || ' / 100.0 * sum(' || case when dataset_month != 0 then 'coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis)' else 'inv.ann_emis' end || ') 
			FROM emissions.' || inv_table_name || ' as inv
			where ' || inv_filter || county_dataset_filter_sql || '
				and poll = ' || quote_literal(target_pollutant)
			into domain_wide_emis_reduction;

			-- update so its viewable for client, via the constraints tab
			execute 'update emf.control_strategy_constraints 
			set domain_wide_emis_reduction = ' || coalesce(domain_wide_emis_reduction || '', 'null::double precision') || '
			where control_strategy_id = ' || int_control_strategy_id;
		ELSE
			execute 'select ' || domain_wide_emis_reduction || ' / sum(' || case when dataset_month != 0 then 'coalesce(inv.avd_emis * ' || no_days_in_month || ', inv.ann_emis)' else 'inv.ann_emis' end || ') * 100.0 
			FROM emissions.' || inv_table_name || ' as inv
			where ' || inv_filter || county_dataset_filter_sql || '
				and poll = ' || quote_literal(target_pollutant)
			into domain_wide_pct_reduction;

			-- update so its viewable for client, via the constraints tab
			execute 'update emf.control_strategy_constraints 
			set domain_wide_pct_reduction = ' || coalesce(domain_wide_pct_reduction || '', 'null::double precision') || '
			where control_strategy_id = ' || int_control_strategy_id;
		END IF;
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

	EXECUTE '
		CREATE TEMP TABLE inv_overrides (
			record_id integer NOT NULL, 
			ceff double precision, 
			reff double precision,
			rpen double precision
		) ON COMMIT DROP;';
		-- fill in record wih missing PM10/PM2_5 ceff...
		EXECUTE 
--		raise notice '%', 
		'insert into inv_overrides (
				record_id, 
				ceff, 
				reff, 
				rpen
			)
		select record_id, missing_ceff, 100.0, 100.0
		from (
			select record_id,
				ceff,
				first_value(ceff) over source_window as missing_ceff,
				sum(case when coalesce(inv.ceff,0.0) = 0.0 then 1 else null end) over source_window as missing_ceff_count,
				sum(1) over source_window as partition_record_count

			from emissions.' || inv_table_name || ' inv

			where ' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
				and inv.poll in (''PM10'',''PM2_5'')

			WINDOW source_window AS (PARTITION BY fips,scc' || case when is_point_table = false then '' else ',plantid,pointid,stackid,segment' end || ' order by fips,scc' || case when is_point_table = false then '' else ',plantid,pointid,stackid,segment' end || ',coalesce(ceff,0.0) desc)
		) foo
		where missing_ceff_count <> partition_record_count
			and coalesce(ceff,0.0) = 0.0;';

	EXECUTE 'CREATE INDEX inv_overrides_record_id ON inv_overrides USING btree (record_id);';


	uncontrolled_emis_sql := public.get_uncontrolled_ann_emis_expression('inv', no_days_in_month, 'inv_ovr', has_rpen_column);
	emis_sql := public.get_ann_emis_expression('inv', no_days_in_month);
	
	annualized_uncontrolled_emis_sql := public.get_uncontrolled_ann_emis_expression('inv', no_days_in_month, 'inv_ovr', has_rpen_column);

	-- build sql that calls ceff SQL equation 
	get_strategty_ceff_equation_sql := public.get_ceff_equation_expression(
		input_dataset_id, -- int_input_dataset_id
		inventory_year, -- inventory_year
		'inv', --inv_table_alias character varying(64), 
		'er');


	percent_reduction_sql := public.get_control_percent_reduction_expression(input_dataset_id,
		inventory_year,
		'inv', 
		no_days_in_month, 
		'inv_ovr', 
		measures_count, 
		'csm', 
		'er');
--	percent_reduction_sql := 'er.efficiency * ' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' * ' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' / 100 / 100';
	-- relative emission reduction from inventory emission (i.e., ann emis in inv 100 tons (add on control gives addtl 50% red --> 50 tons
	-- whereas a 95% ceff replacement control on a source with an existing control of 90% reduction needs to back out source to an 
	-- uncontrolled emission 100 / (1 - 0.9) = 1000 tons giving a and then applying new control gives 950 tons reduced giving a 95% control
	remaining_emis_sql := public.get_remaining_emis_expression(input_dataset_id,
		inventory_year,
		'inv', 
		no_days_in_month, 
		'inv_ovr', 
		measures_count, 
		'csm', 
		'er', 
		has_rpen_column);

	-- get various costing sql expressions
	select annual_cost_expression(cost_expressions),
		capital_cost_expression(cost_expressions),
		operation_maintenance_cost_expression(cost_expressions),
		fixed_operation_maintenance_cost_expression(cost_expressions),
		variable_operation_maintenance_cost_expression(cost_expressions),
		annualized_capital_cost_expression(cost_expressions),
		computed_cost_per_ton_expression(cost_expressions),
		actual_equation_type_expression(cost_expressions)
	from public.get_cost_expressions(
		int_control_strategy_id, -- int_control_strategy_id
		input_dataset_id, -- int_input_dataset_id
		false, --use_override_dataset
		'inv', --inv_table_alias character varying(64), 
		'm', --control_measure_table_alias character varying(64), 
		'et', --equation_type_table_alias character varying(64), 
		'eq', --control_measure_equation_table_alias
		'er', --control_measure_efficiencyrecord_table_alias
		'csm', --control_strategy_measure_table_alias
		'gdplev', --gdplev_table_alias
		'inv_ovr' --inv_override_table_alias
		) as cost_expressions
	into annual_cost_expression,
		capital_cost_expression,
		operation_maintenance_cost_expression,
		fixed_operation_maintenance_cost_expression,
		variable_operation_maintenance_cost_expression,
		annualized_capital_cost_expression,
		computed_cost_per_ton_expression,
		actual_equation_type_expression;


	-- add both target and cobenefit pollutants, first get best target pollutant measure, then use that to apply to other pollutants.
	execute 'insert into emissions.' || worksheet_table_name || ' (
			Dataset_Id,
			cm_abbrev,
			poll,
			scc,
			fips,
			plantid, 
			pointid, 
			stackid, 
			segment, 
			annual_oper_maint_cost,
			annual_variable_oper_maint_cost,
			annual_fixed_oper_maint_cost,
			annualized_capital_cost,
			total_capital_cost,
			annual_cost,
			ann_cost_per_ton,
			control_eff,
			rule_pen,
			rule_eff,
			percent_reduction,
			final_emissions,
			Inv_Ctrl_Eff,
			Inv_Rule_Pen,
			Inv_Rule_Eff,
			emis_reduction,
			inv_emissions,
			input_emis,
			output_emis,
			sic,
			naics,
			source_id,
			input_ds_id,
			cm_id,
			marginal,
			source_annual_cost,
			equation_type,
			original_dataset_id,
			sector,
			source,
			xloc,
			yloc,
			plant,
			REPLACEMENT_ADDON,
			EXISTING_MEASURE_ABBREVIATION,
			EXISTING_PRIMARY_DEVICE_TYPE_CODE
			) 
select 
	' || worksheet_dataset_id || '::integer as dataset_id,
	abbreviation,
	poll,
	scc,
	fips,
	plantid,
	pointid,
	stackid,
	segment,
	operation_maintenance_cost,
	annual_variable_oper_maint_cost,
	annual_fixed_oper_maint_cost,
	annualized_capital_cost,
	capital_cost,
	ann_cost,
	computed_cost_per_ton,
	efficiency,
	rule_pen,
	rule_eff,
	percent_reduction,
	final_emissions,
	ceff,
	rpen,
	reff,
	emis_reduction,
	inv_emissions,
	input_emis,
	output_emis,
	sic,
	naics,
	source_id,
	' || input_dataset_id || '::integer as input_ds_id,
	control_measures_id,
	marginal,
	source_annual_cost,
	equation_type,
	original_dataset_id,
	sector,
	/*null::integer*/source,
	xloc,yloc,
	plant,
	replacement_addon,
	existing_measure_abbr,
	existing_dev_code
--	,source_tp_remaining_emis,source_tp_count,source_annual_cost, winner

from (
--select 
--	*	, rank() OVER (PARTITION BY fips,scc,plantid,pointid,stackid,segment
--			order by fips,scc,plantid,pointid,stackid,segment,source_tp_remaining_emis,coalesce(source_annual_cost,0.0),control_measures_id) as winner
--from (
-- did sum over window here, becuase REQUIRED inner distinct clause was causing the windowing functions to not aggregrate correclty!!!!
select 
	*, 
	sum(ann_cost) OVER w as source_annual_cost,
	case when pollutant_id = ' ||  target_pollutant_id || '::integer and coalesce(emis_reduction, 0) != 0 then coalesce(sum(ann_cost) OVER w / emis_reduction, 0.0) else null::double precision end as marginal,
	sum( case when pollutant_id = ' ||  target_pollutant_id || '::integer then 1 else 0 end ) OVER w as source_tp_count,
	sum(case when pollutant_id = ' ||  target_pollutant_id || '::integer then final_emissions else null::double precision end) OVER w  as source_tp_remaining_emis
from (


		-- get best measures for sources target pollutants (and related cobenefits), there could be a tie for a paticular source
		-- get all matches, dont worry if source doesnt have the target pollutant of interest (subsuquent pass of data will filter on sources with target pollutant if affected)
		
		select DISTINCT ON (inv.record_id,er.control_measures_id) 
--		select DISTINCT ON (inv.fips, inv.scc' || case when is_point_table = false then '' else ', inv.plantid, inv.pointid, inv.stackid, inv.segment' end || ', er.control_measures_id,inv.record_id) 
			p.id as pollutant_id,
			m.abbreviation,
			inv.poll,
			inv.scc,
			inv.fips,
			' || case when is_point_table = false then 'null::character varying(15) as plantid, null::character varying(15) as pointid, null::character varying(15) as stackid, null::character varying(15) as segment, ' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
			' || operation_maintenance_cost_expression || '  as operation_maintenance_cost,
			' || variable_operation_maintenance_cost_expression || '  as annual_variable_oper_maint_cost,
			' || fixed_operation_maintenance_cost_expression || '  as annual_fixed_oper_maint_cost,
			' || annualized_capital_cost_expression || '  as annualized_capital_cost,
			' || capital_cost_expression || ' as capital_cost,
			' || annual_cost_expression || ' as ann_cost,
			' || computed_cost_per_ton_expression || '  as computed_cost_per_ton,
			' || get_strategty_ceff_equation_sql || ' as efficiency,
			' || case when measures_count > 0 then 'coalesce(csm.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' as rule_pen,
			' || case when measures_count > 0 then 'coalesce(csm.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' as rule_eff,
			' || percent_reduction_sql || ' as percent_reduction,
			coalesce(inv_ovr.ceff, inv.ceff) as ceff,
			' || case when is_point_table = false then 'coalesce(inv_ovr.rpen, inv.rpen)' else '100' end || ' as rpen,
			coalesce(inv_ovr.reff, inv.reff) as reff,
			case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * (1 - ' || percent_reduction_sql || ' / 100) as final_emissions,
			' || emis_sql || ' - case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * (1 - ' || percent_reduction_sql || ' / 100) as emis_reduction,
			' || emis_sql || ' as inv_emissions,
			case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end as input_emis,
			case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * (1 - ' || percent_reduction_sql || ' / 100) as output_emis,
			substr(inv.fips, 1, 2) as fipsst,
			substr(inv.fips, 3, 3) as fipscty,
			' || case when has_sic_column = false then 'null::character varying' else 'inv.sic' end || ' as sic,
			' || case when has_naics_column = false then 'null::character varying' else 'inv.naics' end || ' as naics,
			inv.record_id::integer as source_id,
			er.control_measures_id,
			' || coalesce(actual_equation_type_expression, quote_literal('')) || ' as equation_type,
			' || case when has_latlong_columns then 'inv.xloc,inv.yloc,' else 'fipscode.centerlon as xloc,fipscode.centerlat as yloc,' end || '
			' || case when has_plant_column then 'inv.plant' when not has_latlong_columns then 'fipscode.state_county_fips_code_desc as plant' else 'null::character varying as plant' end || ',
			case when coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0 then ''A''
						else ''R'' end as REPLACEMENT_ADDON,
			er.existing_measure_abbr,
			er.existing_dev_code,
			' || quote_literal(strategy_name) || ' as strategy_name,
			ct.name as control_technology,
			sg.name as source_group,
			' || case when not has_merged_columns then 'null::integer as original_dataset_id, ' || quote_literal(sector) || '::character varying as sector' else 'inv.original_dataset_id, inv.sector' end || '
,sources.id as source

		FROM emissions.' || inv_table_name || ' inv

			inner join emf.pollutants p
			on p.name = inv.poll

			inner join emf.sources
			on sources.source = inv.scc || inv.fips || ' || case when is_point_table = false then 'repeat('' '', 60) ' else 'rpad(coalesce(inv.plantid, ''''), 15) || rpad(coalesce(inv.pointid, ''''), 15) || rpad(coalesce(inv.stackid, ''''), 15) || rpad(coalesce(inv.segment, ''''), 15)' end || '

			left outer join inv_overrides inv_ovr
			on inv_ovr.record_id = inv.record_id

			inner join emf.control_measure_sccs scc
			on scc.name = inv.scc
			
			' || case when measures_count > 0 then '
			inner join emf.control_strategy_measures csm
			on csm.control_measure_id = scc.control_measures_id
			and csm.control_strategy_id = ' || int_control_strategy_id || '
			' else '' end || '

			--this part will get applicable measure based on the target pollutant, 
			-- use this measure for target and cobenefit pollutants...
			inner join emf.control_measures m
			on m.id = scc.control_measures_id

-- for Non CoST SUs, make sure they only see their temporary measures
			' || case when not is_cost_su then '

			inner join emf.control_measure_classes cmc
			on cmc.id = m.cm_class_id
			and (
				(cmc.name = ''Temporary'' and m.creator = ' || creator_user_id || ')
				or (cmc.name <> ''Temporary'')
			)
			' else '' end || '

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
			and ' || annualized_uncontrolled_emis_sql || ' between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
--					and source_total.total_ann_emis between coalesce(er.min_emis, -1E+308) and coalesce(er.max_emis, 1E+308)
			-- locale filter
			and (er.locale = inv.fips or er.locale = substr(inv.fips, 1, 2) or er.locale = '''')
			-- effecive date filter
			and ' || inventory_year || '::integer >= coalesce(date_part(''year'', er.effective_date), ' || inventory_year || '::integer)		
			-- Replacement vs Add On Logic...
			and (

				-- Measure is Add On Only!
				(
					(coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0)
					and 
					(
						(length(inv.control_measures) > 0 and strpos(''&'' || coalesce(inv.control_measures, '''') || ''&'', ''&'' || er.existing_measure_abbr || ''&'') > 0) '
						|| case when has_cpri_column then ' or (inv.cpri <> 0 and er.existing_dev_code = inv.cpri) '
						when has_primary_device_type_code_column then ' or (length(inv.primary_device_type_code) > 0 and er.existing_dev_code || '''' = inv.primary_device_type_code) '
						else '' end || '
					)
				)

				-- Measure is Replacement Only!
				or 
				(
					coalesce(er.existing_measure_abbr, '''') = '''' and coalesce(er.existing_dev_code, 0) = 0
				)


			)


			left outer join reference.gdplev gdplev_incr
			on gdplev_incr.annual = er.cost_year

			' || case when measures_count = 0 and measure_classes_count > 0 then '
			inner join emf.control_strategy_classes csc
			on csc.control_measure_class_id = m.cm_class_id
			and csc.control_strategy_id = ' || int_control_strategy_id || '
			' else '' end || '

			-- target pollutant filter
--					inner join emf.control_strategy_target_pollutants cstp
--					on cstp.pollutant_id = p.id
--					and cstp.control_strategy_id = ' || int_control_strategy_id || '

			left outer join emf.control_measure_properties ceff_et
			on ceff_et.control_measure_id = m.id
			and ceff_et."name" = ''CEFF_EQUATION_'' || inv.poll || ''_TYPE''

			left outer join emf.control_measure_properties ceff_var1
			on ceff_var1.control_measure_id = m.id
			and ceff_var1."name" = ''CEFF_EQUATION_'' || inv.poll || ''_VAR1''

			left outer join emf.control_measure_properties ceff_var2
			on ceff_var2.control_measure_id = m.id
			and ceff_var2."name" = ''CEFF_EQUATION_'' || inv.poll || ''_VAR2''

			' || case when not has_latlong_columns then 'left outer join reference.fips fipscode
			on fipscode.state_county_fips = inv.fips
			and fipscode.country_num = ''0''' else '' end || '

			left outer join emf.control_technologies ct
			on ct.id = m.control_technology

			left outer join emf.source_groups sg
			on sg.id = m.source_group

		where 	' || inv_filter || coalesce(county_dataset_filter_sql, '') || '
			--and p.id = ' ||  target_pollutant_id || '

			-- dont include sources that have no emissions...
			and ' || uncontrolled_emis_sql || ' <> 0.0

			-- dont include measures with no specified ceff (look for null values)...
			and ' || get_strategty_ceff_equation_sql || ' is not null

			-- only relevant for target pollutant
			and (
				(p.id = ' ||  target_pollutant_id || '

				-- dont include sources that have been fully controlled...
				and coalesce(100 * coalesce(inv_ovr.ceff, inv.ceff) / 100 * coalesce(coalesce(inv_ovr.reff, inv.reff) / 100, 1.0)' || case when has_rpen_column then ' * coalesce(coalesce(inv_ovr.rpen, inv.rpen) / 100, 1.0)' else '' end || ', 0) <> 100.0


				-- make sure the new control is worthy
				-- this is only relevant for Replacement controls, not Add-on controls or sources with no existing control
				and (
					-- source has no existing control
					(
						coalesce(inv_ovr.ceff, inv.ceff, 0.0) = 0.0
					)
					-- control is add-on type
					or (
						(coalesce(er.existing_measure_abbr, '''') <> '''' or er.existing_dev_code <> 0)
					)
					-- replacement control that meets the constraint, source has existing control
					or (
						coalesce(inv_ovr.ceff, inv.ceff, 0.0) <> 0.0
						and (coalesce(er.existing_measure_abbr, '''') = '''' and coalesce(er.existing_dev_code, 0) = 0)
						and ((' ||  emis_sql || ') - (' || remaining_emis_sql || ')) / (' ||  emis_sql || ') * 100 >= ' || replacement_control_min_eff_diff_constraint || '
					)
				) 

				-- dont include measures already on the source...
				and strpos(''&'' || coalesce(inv.control_measures, '''') || ''&'', ''&'' || m.abbreviation || ''&'') = 0

				-- constraints filter
				' || case when has_constraints then '
				and (
						' || percent_reduction_sql || ' >= ' || coalesce(min_control_efficiency_constraint, -100.0) || '
						' || coalesce(' and (' ||  emis_sql || ') - (' || remaining_emis_sql || ') >= ' || min_emis_reduction_constraint, '')  || '
						' || coalesce(' and coalesce(' || computed_cost_per_ton_expression || ', -1E+308) <= ' || max_cost_per_ton_constraint, '')  || '
						' || coalesce(' and coalesce(' || annual_cost_expression || ', -1E+308) <= ' || max_ann_cost_constraint, '')  || '
				)' else '' end || ')
				or
				p.id <> ' ||  target_pollutant_id || '

			)
			
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

		order by inv.record_id,
			er.control_measures_id, 
--		order by inv.fips, 
--			inv.scc, ' || case when not is_point_table then '' else 'inv.plantid, inv.pointid, inv.stackid, inv.segment, ' end || '
--			er.control_measures_id, inv.record_id,
			case when length(er.locale) = 5 then 0 when length(er.locale) = 2 then 1 else 2 end, 
			' || remaining_emis_sql || ',
			(' || annual_cost_expression || ') 
	) foo

	WINDOW w AS (PARTITION BY fips,scc,plantid,pointid,stackid,segment,control_measures_id)
	) foo
where source_tp_count > 0	-- this limits to only measures that actually controlled the target pollutant
 ' || case 
	when include_unspecified_costs then '' 
	else 'and source_annual_cost is not null	-- this limits to only measures with actual costs'
end || '
--	) foo
--	where winner = 1
	';

	return;
END;
$$ LANGUAGE plpgsql;

