CREATE OR REPLACE FUNCTION public.get_cost_expressions(
	int_control_strategy_id integer,
	int_input_dataset_id integer,
	use_override_dataset boolean,
	inv_table_alias character varying(64), 
	control_measure_table_alias character varying(64), 
	equation_type_table_alias character varying(64), 
	control_measure_equation_table_alias character varying(64), 
	control_measure_efficiencyrecord_table_alias character varying(64), 
	control_strategy_measure_table_alias character varying(64), 
	gdplev_table_alias character varying(64), 
	inv_override_table_alias character varying(64), 
	OUT annual_cost_expression text, 
	OUT capital_cost_expression text, 
	OUT operation_maintenance_cost_expression text, 
	OUT fixed_operation_maintenance_cost_expression text, 
	OUT variable_operation_maintenance_cost_expression text, 
	OUT annualized_capital_cost_expression text, 
	OUT computed_cost_per_ton_expression text, 
	OUT actual_equation_type_expression text)  AS $$
DECLARE
--	annualized_uncontrolled_emis_sql character varying;
	inv_table_name character varying;
	uncontrolled_emis_sql character varying;
	emis_sql character varying;
	percent_reduction_sql character varying;
	remaining_emis_sql character varying;
	emis_reduction_sql character varying;
	get_strategty_ceff_equation_sql character varying;
	measures_count integer := 0;
	dataset_month smallint := 0;
	no_days_in_month smallint := 31;

	use_cost_equations boolean;
	discount_rate double precision;

	is_point_table boolean := false; 
	has_design_capacity_columns boolean := false; 
	has_rpen_column boolean := false;
--	has_cpri_column boolean := false; 
--	has_primary_device_type_code_column boolean := false; 

	ref_cost_year integer := 2006;
	cost_year_chained_gdp double precision := null;
	ref_cost_year_chained_gdp double precision := null;
	chained_gdp_adjustment_factor double precision := null;
	cost_year integer := null;
BEGIN
	-- see if control strategy has only certain measures specified
	SELECT count(id)
	FROM emf.control_strategy_measures 
	where control_strategy_measures.control_strategy_id = int_control_strategy_id 
	INTO measures_count; 

	-- get the input dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = int_input_dataset_id
	into inv_table_name;

	-- get target pollutant, inv filter, and county dataset info if specified
	SELECT cs.use_cost_equations,
		cs.discount_rate / 100,
		cs.cost_year
	FROM emf.control_strategies cs
	where cs.id = int_control_strategy_id
	INTO use_cost_equations,
		discount_rate,
		cost_year;

	-- see if there are point specific columns in the inventory
	is_point_table := public.check_table_for_columns(inv_table_name, 'plantid,pointid,stackid,segment', ',');

	-- see if there is a rpen column in the inventory
	has_rpen_column := public.check_table_for_columns(inv_table_name, 'rpen', ',');

	-- see if there is design capacity columns in the inventory
	has_design_capacity_columns := public.check_table_for_columns(inv_table_name, 'design_capacity,design_capacity_unit_numerator,design_capacity_unit_denominator', ',');

	-- see if there is plant column in the inventory
	--has_cpri_column := public.check_table_for_columns(inv_table_name, 'cpri', ',');

	-- see if there is primary_device_type_code column in the inventory
	--has_primary_device_type_code_column := public.check_table_for_columns(inv_table_name, 'primary_device_type_code', ',');

	-- get month of the dataset, 0 (Zero) indicates an annual inventory
	select public.get_dataset_month(int_input_dataset_id)
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

	uncontrolled_emis_sql := '(' ||
			case 
				when dataset_month != 0 then 
					'case when (1 - coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.ceff' else 'coalesce(' || inv_table_alias || '.ceff, ' || inv_override_table_alias || '.ceff)' end || ' / 100 * coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.reff' else 'coalesce(' || inv_table_alias || '.reff, ' || inv_override_table_alias || '.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.rpen' else 'coalesce(' || inv_table_alias || '.rpen, ' || inv_override_table_alias || '.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(' || inv_table_alias || '.avd_emis * ' || no_days_in_month || ', ' || inv_table_alias || '.ann_emis) / (1 - coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.ceff' else 'coalesce(' || inv_table_alias || '.ceff, ' || inv_override_table_alias || '.ceff)' end || ' / 100 * coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.reff' else 'coalesce(' || inv_table_alias || '.reff, ' || inv_override_table_alias || '.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.rpen' else 'coalesce(' || inv_table_alias || '.rpen, ' || inv_override_table_alias || '.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
				else 
					'case when (1 - coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.ceff' else 'coalesce(' || inv_table_alias || '.ceff, ' || inv_override_table_alias || '.ceff)' end || ' / 100 * coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.reff' else 'coalesce(' || inv_table_alias || '.reff, ' || inv_override_table_alias || '.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.rpen' else 'coalesce(' || inv_table_alias || '.rpen, ' || inv_override_table_alias || '.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then ' || inv_table_alias || '.ann_emis / (1 - coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.ceff' else 'coalesce(' || inv_table_alias || '.ceff, ' || inv_override_table_alias || '.ceff)' end || ' / 100 * coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.reff' else 'coalesce(' || inv_table_alias || '.reff, ' || inv_override_table_alias || '.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.rpen' else 'coalesce(' || inv_table_alias || '.rpen, ' || inv_override_table_alias || '.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end' 
			end || ')';
	emis_sql := 
			case 
				when dataset_month != 0 then 
					'coalesce(' || inv_table_alias || '.avd_emis * ' || no_days_in_month || ', ' || inv_table_alias || '.ann_emis)' 
				else 
					'' || inv_table_alias || '.ann_emis' 
			end;
	
/*	annualized_uncontrolled_emis_sql := 
			case 
				when dataset_month != 0 then 
					'case when (1 - coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.ceff' else 'coalesce(' || inv_table_alias || '.ceff, ' || inv_override_table_alias || '.ceff)' end || ' / 100 * coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.reff' else 'coalesce(' || inv_table_alias || '.reff, ' || inv_override_table_alias || '.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.rpen' else 'coalesce(' || inv_table_alias || '.rpen, ' || inv_override_table_alias || '.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then coalesce(' || inv_table_alias || '.avd_emis * 365, ' || inv_table_alias || '.ann_emis) / (1 - coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.ceff' else 'coalesce(' || inv_table_alias || '.ceff, ' || inv_override_table_alias || '.ceff)' end || ' / 100 * coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.reff' else 'coalesce(' || inv_table_alias || '.reff, ' || inv_override_table_alias || '.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.rpen' else 'coalesce(' || inv_table_alias || '.rpen, ' || inv_override_table_alias || '.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
				else 
					'case when (1 - coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.ceff' else 'coalesce(' || inv_table_alias || '.ceff, ' || inv_override_table_alias || '.ceff)' end || ' / 100 * coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.reff' else 'coalesce(' || inv_table_alias || '.reff, ' || inv_override_table_alias || '.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.rpen' else 'coalesce(' || inv_table_alias || '.rpen, ' || inv_override_table_alias || '.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) != 0 then ' || inv_table_alias || '.ann_emis / (1 - coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.ceff' else 'coalesce(' || inv_table_alias || '.ceff, ' || inv_override_table_alias || '.ceff)' end || ' / 100 * coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.reff' else 'coalesce(' || inv_table_alias || '.reff, ' || inv_override_table_alias || '.reff)' end || ' / 100, 1.0)' || case when has_rpen_column then ' * coalesce(' || case when not use_override_dataset then '' || inv_table_alias || '.rpen' else 'coalesce(' || inv_table_alias || '.rpen, ' || inv_override_table_alias || '.rpen)' end || ' / 100, 1.0)' else '' end || ', 0)) else 0.0::double precision end'
			end;
*/

	-- build sql that calls ceff SQL equation 
	get_strategty_ceff_equation_sql := '(public.get_strategy_ceff_equation(ceff_et."value"::character varying(255), (' || emis_sql || ')::double precision, ' || case when not is_point_table then 'null::double precision, null::double precision, null::double precision' else '(' || inv_table_alias || '.stkflow * 60.0)::double precision, ' || inv_table_alias || '.stktemp::double precision, ' || inv_table_alias || '.annual_avg_hours_per_year::double precision' end || ',' || control_measure_efficiencyrecord_table_alias || '.efficiency, ceff_var1."value"::double precision, ceff_var2."value"::double precision))';

	get_strategty_ceff_equation_sql := '
					(case 
						--Equation Type 1 
						when ' || is_point_table || '::boolean and coalesce(ceff_et."value",'''') = ''Type 1'' and coalesce(' || inv_table_alias || '.stkflow, 0) > 0 AND coalesce(' || inv_table_alias || '.stktemp, 0) > 0 AND coalesce(' || inv_table_alias || '.annual_avg_hours_per_year, 0) > 0 and coalesce((' || emis_sql || '),0.0) <> 0.0 then'
							/*
								Step 1a) Convert NEI Stack Velocity to volumetric flow rate under actual stack conditions (actual cubic ft per minute):

								Step 1b) Convert Actual Vol. flow rate to Standard Vol flow rate (scfm):

								Step 1c) Calculate NOx outlet concentration:

								-- Step 1b
								std_volumetric_flow_rate := stack_flow_rate * 520 / (stack_temperature + 460.0);

								-- Step 1c
								ceff := ((outlet_concentration * std_volumetric_flow_rate / 10 ^ 6 / ((0.7302 * 520) / (1)) * 60 * annual_avg_hours_per_year * pollutant_molecular_weight / 2000) / ann_emis) * 100;
							*/|| '
							((' || emis_sql || ') -
								(
									(ceff_var2."value"::double precision)/*outlet_concentration*/ 
									* (
										(' || inv_table_alias || '.stkflow * 60.0) 
										* 520 
										/ (
											(' || inv_table_alias || '.stktemp) 
											+ 460.0
										)
									) 
									/ 10 ^ 6 
									/ (
										(0.7302 * 520) 
										/ 
										(1)
									) 
									* 60 
									* ' || inv_table_alias || '.annual_avg_hours_per_year 
									* (ceff_var1."value"::double precision)/*pollutant_molecular_weight*/ 
									/ 2000
								) 
								/ (' || emis_sql || ')
							) 
							* 100
						--Default to efficiencyrecord ceff
						else
							' || control_measure_efficiencyrecord_table_alias || '.efficiency
					end)';


	percent_reduction_sql := '(' || get_strategty_ceff_equation_sql || '

	
		* ' || case when measures_count > 0 then 'coalesce(' || control_strategy_measure_table_alias || '.rule_effectiveness, ' || control_measure_efficiencyrecord_table_alias || '.rule_effectiveness)' else '' || control_measure_efficiencyrecord_table_alias || '.rule_effectiveness' end || ' * ' || case when measures_count > 0 then 'coalesce(' || control_strategy_measure_table_alias || '.rule_penetration, ' || control_measure_efficiencyrecord_table_alias || '.rule_penetration)' else '' || control_measure_efficiencyrecord_table_alias || '.rule_penetration' end || ' / 100 / 100)';

	
--	percent_reduction_sql := 'er.efficiency * ' || case when measures_count > 0 then 'coalesce(' || control_strategy_measure_table_alias || '.rule_effectiveness, er.rule_effectiveness)' else 'er.rule_effectiveness' end || ' * ' || case when measures_count > 0 then 'coalesce(' || control_strategy_measure_table_alias || '.rule_penetration, er.rule_penetration)' else 'er.rule_penetration' end || ' / 100 / 100';
	remaining_emis_sql := 
		'( case when coalesce(' || control_measure_efficiencyrecord_table_alias || '.existing_measure_abbr, '''') <> '''' or ' || control_measure_efficiencyrecord_table_alias || '.existing_dev_code <> 0 then -- add on control
			' || emis_sql || ' * ' || percent_reduction_sql || ' / 100.0 
		else -- replacement control
			(' || uncontrolled_emis_sql || ' * (1.0 - ' || percent_reduction_sql || ' / 100.0))
		end )';

	emis_reduction_sql := '(case when coalesce(' || control_measure_efficiencyrecord_table_alias || '.existing_measure_abbr, '''') <> '''' or ' || control_measure_efficiencyrecord_table_alias || '.existing_dev_code <> 0 then ' || emis_sql || ' else ' || uncontrolled_emis_sql || ' end * ' || percent_reduction_sql || ' / 100)::double precision';
		
	-- prepare annual_cost_expression 
	annual_cost_expression := 
	case 
		when use_cost_equations then 
			'(case 

				--Equation Type 1 
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 1'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
					annual_cost := annualized_capital_cost + operation_maintenance_cost
					annualized_capital_cost := capital_cost * cap_recovery_factor
					capital_cost := capital_cost_multiplier * design_capacity * scaling_factor * 1000
					operation_maintenance_cost := coalesce(fixed_operation_maintenance_cost, 0) + coalesce(variable_operation_maintenance_cost, 0)
					fixed_operation_maintenance_cost := fixed_om_cost_multiplier * design_capacity * 1000;
					variable_operation_maintenance_cost := variable_om_cost_multiplier * design_capacity * capacity_factor * 8760;
						scaling_factor := 
							case 
								when (measure_abbreviation = ''NSCR_UBCW'' or measure_abbreviation = ''NSCR_UBCT'') and design_capacity >= 600.0 then 1.0
								when design_capacity >= 500.0 then 1.0
								else scaling_factor_model_size ^ scaling_factor_exponent
							end;
						IF coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 THEN 
						     cap_recovery_factor := public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life);
						END IF;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					/*annualized_capital_cost*/ 
					(' || control_measure_equation_table_alias || '.value1/*capital_cost_multiplier*/ * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) 
					* case 
						when (' || control_measure_table_alias || '.abbreviation = ''NSCR_UBCW'' or ' || control_measure_table_alias || '.abbreviation = ''NSCR_UBCT'') and public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) >= 600.0 then 1.0
						when public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) >= 500.0 then 1.0
						else ' || control_measure_equation_table_alias || '.value4/*scaling_factor_model_size*/ ^ ' || control_measure_equation_table_alias || '.value5/*scaling_factor_exponent*/
					end /*scaling_factor*/
					* 1000) /*capital_cost*/
					* case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end /*cap_recovery_factor*/
					+ 
					/*operation_maintenance_cost*/
					coalesce(' || control_measure_equation_table_alias || '.value2/*fixed_om_cost_multiplier*/ * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) * 1000/*fixed_operation_maintenance_cost*/, 0) 
					+ coalesce(' || control_measure_equation_table_alias || '.value3/*variable_om_cost_multiplier*/ * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) * ' || control_measure_equation_table_alias || '.value6/*capacity_factor*/ * 8760/*variable_operation_maintenance_cost*/, 0)
					)

				--Equation Type 2 
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 2'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 and coalesce(3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')), 0) <= 2000.0 then '
					/*
						-- calculate capital cost
						capital_cost := capital_cost_multiplier * (design_capacity ^ capital_cost_exponent);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := annual_cost_multiplier * design_capacity ^ annual_cost_exponent;

						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					--annual_cost := annual_cost_multiplier * ' || inv_table_alias || '.design_capacity ^ annual_cost_exponent;
					(case when coalesce(' || inv_table_alias || '.ceff, 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value3 else ' || control_measure_equation_table_alias || '.value7 end)/*annual_cost_multiplier*/ * ((3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, ''''))/*design_capacity*/) ^ (case when coalesce(' || inv_table_alias || '.ceff, 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value4 else ' || control_measure_equation_table_alias || '.value8 end)/*annual_cost_exponent*/)
					)

				--Equation Type 3 
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 3'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*

						cap_recovery_factor double precision := capital_recovery_factor;
						capital_cost_factor double precision := 192;
						gas_flow_rate_factor double precision := 0.486;
						retrofit_factor double precision := 1.1;

						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(1028000/ stack_flow_rate) ^ 0.6 * capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
								else
									capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
							end * 0.9383;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- 	Fixed O&M = Gas Flow Rate Factor * Fixed O&M Rate
						-- 		where Gas Flow Rate Factor = 0.486 kW/acfm and Fixed O&M Rate = $6.9/kW-yr.
						fixed_operation_maintenance_cost := 0.486 * 6.9 * stack_flow_rate;

						-- 	Variable O&M = Gas Flow Rate Factor * Variable O&M Rate * Hours per Year * STKFLOW * 60
						-- 		where Gas Flow Rate Factor = 0.486 kW/acf; Variable O&M Rate = $0.0015/kWh; 
						--		Hours per Year = 8,736 hours, STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						variable_operation_maintenance_cost := 0.486 * 0.0015 * 8736 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (3.35 + (0.000729 * 8736)) * stack_flow_rate * 0.9383; (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;

					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(
					case 
						when (' || inv_table_alias || '.stkflow * 60.0) < 1028000 then (1028000/ (' || inv_table_alias || '.stkflow * 60.0)) ^ 0.6
						else 1.0
					end * 192/*capital_cost_factor*/ * 0.486/*gas_flow_rate_factor*/ * 1.1/*retrofit_factor*/ * (' || inv_table_alias || '.stkflow * 60.0) * 0.9383/*capital_cost*/
					* 
					case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end/*cap_recovery_factor*/)/*annualized_capital_cost*/ 
					+ (
					(0.486 * 6.9 * (' || inv_table_alias || '.stkflow * 60.0))/*fixed_operation_maintenance_cost*/
					+ (0.486 * 0.0015 * 8736 * (' || inv_table_alias || '.stkflow * 60.0))/*variable_operation_maintenance_cost*/
					)/*operation_maintenance_cost*/
					)

				--Equation Type 4
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 4'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (990000 + 9.836 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $75,800
						--		where $75,800 is the fixed O&M cost based on model plant data
						fixed_operation_maintenance_cost := 75800;
						
						--	Variable O&M = $12.82 * STKFLOW * 60
						--		where $12.82 is the variable O&M cost based on model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 12.82 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (75800 + 12.82 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(990000 + 9.836 * (' || inv_table_alias || '.stkflow * 60.0))/*capital_cost*/ 
					* case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end/*cap_recovery_factor*//*annualized_capital_cost*/ 
					+ (
					75800/*fixed_operation_maintenance_cost*/ + (12.82 * (' || inv_table_alias || '.stkflow * 60.0))/*variable_operation_maintenance_cost*/
					)/*operation_maintenance_cost*/
					)

				--Equation Type 5
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 5'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (2882540 + 244.74 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $749,170
						--		where $749,170 is the fixed O&M cost based on model plant data,
						fixed_operation_maintenance_cost := 749170;

						--	Variable O&M=$148.4 * STKFLOW * 60
						--		where $148.4 is the variable O&M data based on model plant data and credit for recovered product, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 148.40 * stack_flow_rate;
						
						-- 	calculate operation maintenance cost
						--	operation_maintenance_cost := (749170 + 148.40 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(2882540 + 244.74 * (' || inv_table_alias || '.stkflow * 60.0))/*capital_cost*/ 
					* case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end/*cap_recovery_factor*//*annualized_capital_cost*/ 
					+ (
					749170/*fixed_operation_maintenance_cost*/ + (148.40 * (' || inv_table_alias || '.stkflow * 60.0))/*variable_operation_maintenance_cost*/
					)/*operation_maintenance_cost*/
					)

				--Equation Type 6
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 6'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (3449803 + 135.86 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $797,667
						--		where $797,667 is the fixed O&M cost derived from model plant data
						fixed_operation_maintenance_cost := 797667;

						--	Variable O&M = $58.84 * STKFLOW * 60
						--		where $58.84 is the variable O&M cost derived from model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 58.84 * stack_flow_rate;

						-- calculate operation maintenance cost
						--	operation_maintenance_cost := (797667 + 58.84 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(3449803 + 135.86 * (' || inv_table_alias || '.stkflow * 60.0))/*capital_cost*/ 
					* case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end/*cap_recovery_factor*//*annualized_capital_cost*/ 
					+ (
					797667/*fixed_operation_maintenance_cost*/ + (58.84 * (' || inv_table_alias || '.stkflow * 60.0))/*variable_operation_maintenance_cost*/
					)/*operation_maintenance_cost*/
					)

				--Equation Type 7
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 7'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(2882540 + (244.74 * stack_flow_rate) + (((1028000 / stack_flow_rate) ^ 0.6)) * 93.3 * 1.1 * stack_flow_rate * 0.9383)
								else
									(2882540 + (244.74 * stack_flow_rate) + 93.3 * 1.1 * stack_flow_rate * 0.9383)
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate operation maintenance cost
						operation_maintenance_cost := (749170 + (148.40 * stack_flow_rate) + (3.35 + (0.000729 * 8736 ) * (stack_flow_rate) ^ 0.9383));

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					case 
						when (' || inv_table_alias || '.stkflow * 60.0) < 1028000 then 
							(2882540 + (244.74 * (' || inv_table_alias || '.stkflow * 60.0)) + (((1028000 / (' || inv_table_alias || '.stkflow * 60.0)) ^ 0.6)) * 93.3 * 1.1 * (' || inv_table_alias || '.stkflow * 60.0) * 0.9383)
						else
							(2882540 + (244.74 * (' || inv_table_alias || '.stkflow * 60.0)) + 93.3 * 1.1 * (' || inv_table_alias || '.stkflow * 60.0) * 0.9383)
					end/*capital_cost*/ 
					* case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end/*cap_recovery_factor*//*annualized_capital_cost*/ 
					+ (749170 + (148.40 * (' || inv_table_alias || '.stkflow * 60.0)) + (3.35 + (0.000729 * 8736 ) * ((' || inv_table_alias || '.stkflow * 60.0)) ^ 0.9383))/*operation_maintenance_cost*/
					)

				--Equation Type 8
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 8'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then capital_control_cost_factor * stack_flow_rate
								else default_capital_cpt_factor * emis_reduction
							end;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then om_control_cost_factor * stack_flow_rate
								else default_om_cpt_factor * emis_reduction
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then annualized_capital_cost + 0.04 * capital_cost + operation_maintenance_cost
								else default_annualized_cpt_factor * emis_reduction
							end;

					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					case 
						when (' || inv_table_alias || '.stkflow * 60.0) >= 5.0 then 
							/*capital_cost*/case 
								when (' || inv_table_alias || '.stkflow * 60.0) >= 5.0 then ' || control_measure_equation_table_alias || '.value1/*capital_control_cost_factor*/ * (' || inv_table_alias || '.stkflow * 60.0)
								else ' || control_measure_equation_table_alias || '.value3/*default_capital_cpt_factor*/ * ' || emis_reduction_sql || '
							end 
							* case 
								when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
								else cap_rec_factor
							end/*cap_recovery_factor*/ 
							+ 0.04 * 
							/*capital_cost*/case 
								when (' || inv_table_alias || '.stkflow * 60.0) >= 5.0 then ' || control_measure_equation_table_alias || '.value1/*capital_control_cost_factor*/ * (' || inv_table_alias || '.stkflow * 60.0)
								else ' || control_measure_equation_table_alias || '.value3/*default_capital_cpt_factor*/ * ' || emis_reduction_sql || '
							end 
							+ 
							/*operation_maintenance_cost*/case 
								when (' || inv_table_alias || '.stkflow * 60.0) >= 5.0 then ' || control_measure_equation_table_alias || '.value2/*om_control_cost_factor*/ * (' || inv_table_alias || '.stkflow * 60.0)
								else ' || control_measure_equation_table_alias || '.value4/*default_om_cpt_factor*/ * ' || emis_reduction_sql || '
							end
						else
							' || control_measure_equation_table_alias || '.value5/*default_annualized_cpt_factor*/ * ' || emis_reduction_sql || '
					end
					)

				-- Equation Type 9
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 9'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := ((total_equipment_cost_factor * stack_flow_rate) + total_equipment_cost_constant) * equipment_to_capital_cost_multiplier;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							((electricity_factor * stack_flow_rate) + electricity_constant) + ((dust_disposal_factor * stack_flow_rate) + dust_disposal_constant) + ((bag_replacement_factor * stack_flow_rate) + bag_replacement_constant);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					((((' || control_measure_equation_table_alias || '.value1/*total_equipment_cost_factor*/ * (' || inv_table_alias || '.stkflow * 60.0)) + ' || control_measure_equation_table_alias || '.value2/*total_equipment_cost_constant*/) * ' || control_measure_equation_table_alias || '.value3/*equipment_to_capital_cost_multiplier*/)/*capital_cost*/ * case 
								when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
								else cap_rec_factor
							end/*cap_recovery_factor*/)/*annualized_capital_cost*/ 
					+ (((' || control_measure_equation_table_alias || '.value4/*electricity_factor*/ * (' || inv_table_alias || '.stkflow * 60.0)) + ' || control_measure_equation_table_alias || '.value5/*electricity_constant*/) + ((' || control_measure_equation_table_alias || '.value6/*dust_disposal_factor*/ * (' || inv_table_alias || '.stkflow * 60.0)) + ' || control_measure_equation_table_alias || '.value7/*dust_disposal_constant*/) + ((' || control_measure_equation_table_alias || '.value8/*bag_replacement_factor*/ * (' || inv_table_alias || '.stkflow * 60.0)) + ' || control_measure_equation_table_alias || '.value9/*bag_replacement_constant*/))/*operation_maintenance_cost*/
					)

				-- Equation Type 10
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 10'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := design_capacity * capital_cost_multiplier * 1000 * (250.0 / design_capacity) ^ capital_cost_exponent;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;


						-- calculate variable_operation_maintenance_cost 
						variable_operation_maintenance_cost := variable_operation_maintenance_cost_multiplier * design_capacity * 0.85 * annual_avg_hours_per_year;
						
						-- calculate fixed_operation_maintenance_cost
						fixed_operation_maintenance_cost := design_capacity * 1000 * fixed_operation_maintenance_cost_multiplier * (250 / design_capacity) ^ fixed_operation_maintenance_cost_exponent;

					--O2 *1000*0.24*(250/O2)^0.3

						-- calculate operation maintenance cost
						operation_maintenance_cost := variable_operation_maintenance_cost + fixed_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					((public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, 
							case 
								when length(coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, '''')) = 0 then 
									''MW''::character varying
								else
									' || inv_table_alias || '.design_capacity_unit_numerator
							end
							, coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) * ' || control_measure_equation_table_alias || '.value1/*capital_cost_multiplier*/ * 1000 * (250.0 / public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, 
							case 
								when length(coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, '''')) = 0 then 
									''MW''::character varying
								else
									' || inv_table_alias || '.design_capacity_unit_numerator
							end
							, coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, ''''))) ^ ' || control_measure_equation_table_alias || '.value2/*capital_cost_exponent*/)/*capital_cost*/ * case 
								when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
								else cap_rec_factor
							end/*cap_recovery_factor*/)/*annualized_capital_cost*/ 
					+ ((' || control_measure_equation_table_alias || '.value3/*variable_operation_maintenance_cost_multiplier*/ * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, 
							case 
								when length(coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, '''')) = 0 then 
									''MW''::character varying
								else
									' || inv_table_alias || '.design_capacity_unit_numerator
							end
							, coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) * 0.85 * ' || inv_table_alias || '.annual_avg_hours_per_year)/*variable_operation_maintenance_cost*/ + (public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, 
							case 
								when length(coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, '''')) = 0 then 
									''MW''::character varying
								else
									' || inv_table_alias || '.design_capacity_unit_numerator
							end
							, coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) * 1000 * ' || control_measure_equation_table_alias || '.value4/*fixed_operation_maintenance_cost_multiplier*/ * (250 / public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, 
							case 
								when length(coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, '''')) = 0 then 
									''MW''::character varying
								else
									' || inv_table_alias || '.design_capacity_unit_numerator
							end
							, coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, ''''))) ^ ' || control_measure_equation_table_alias || '.value5/*fixed_operation_maintenance_cost_exponent*/)/*fixed_operation_maintenance_cost*/)/*operation_maintenance_cost*/

					)

				-- Equation Type 11
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 11'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
						-- figure out cost per ton
						computed_cost_per_ton := 
							case 
								when design_capacity <= low_boiler_capacity_range then low_default_cost_per_ton
								when design_capacity > low_boiler_capacity_range and design_capacity < medium_boiler_capacity_range then medium_default_cost_per_ton
								when design_capacity >= medium_boiler_capacity_range then high_default_cost_per_ton
							end;

						-- calculate annual cost
						annual_cost := emis_reduction * computed_cost_per_ton;
						-- calculate capital cost
						capital_cost := annual_cost  * capital_annualized_ratio;
						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;
						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					' || emis_reduction_sql || ' 
					* (case 
						when 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) <= ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value1/*low_default_cost_per_ton*/
						when 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) > ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ and 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) < ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value3/*medium_default_cost_per_ton*/
						when 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) >= ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value5/*high_default_cost_per_ton*/
					end)/*computed_cost_per_ton*/
					)
					
				-- Equation Type 12
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 12'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 and coalesce(160.0/*stktemp*/, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (coalesce(total_capital_investment_fixed_factor, 0.0) + coalesce(total_capital_investment_variable_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000) ^ 0.6;

						-- calculate fixed operation maintenance cost
						fixed_operation_maintenance_cost := (coalesce(annual_operating_cost_fixed_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate variable operation maintenance cost
						variable_operation_maintenance_cost := (coalesce(annual_operating_cost_variable_factor,0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate operation maintenance cost
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := operation_maintenance_cost + annualized_capital_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(
						(coalesce(' || control_measure_equation_table_alias || '.value3/*annual_operating_cost_fixed_factor*/, 0.0)) * ((' || inv_table_alias || '.stkflow * 60.0/*stack_flow_rate*/ * 520 / (160.0/*stktemp*//*stack_temperature*/ + 460.0)) / 150000)
						+ (coalesce(' || control_measure_equation_table_alias || '.value4/*annual_operating_cost_variable_factor*/, 0.0)) * ((' || inv_table_alias || '.stkflow * 60.0/*stack_flow_rate*/ * 520 / (160.0/*stktemp*//*stack_temperature*/ + 460.0)) / 150000)
					)/*operation_maintenance_cost*/
					+ (coalesce(' || control_measure_equation_table_alias || '.value1/*total_capital_investment_fixed_factor*/, 0.0) + coalesce(' || control_measure_equation_table_alias || '.value2/*total_capital_investment_variable_factor*/, 0.0)) * ((' || inv_table_alias || '.stkflow * 60.0/*stack_flow_rate*/ * 520 / (160.0/*stktemp*//*stack_temperature*/ + 460.0)) / 150000) ^ 0.6 * case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end/*cap_recovery_factor*//*annualized_capital_cost*/
					)
				else 
			' else '
		' end || '
					' || emis_reduction_sql || ' * ' || control_measure_efficiencyrecord_table_alias || '.ref_yr_cost_per_ton
		' || case 
			when use_cost_equations then '
			end 
			' else '
		' end || ')';

	-- prepare capital_cost_expression 
	capital_cost_expression := 
	case 
		when use_cost_equations then 
			'(case 

				--Equation Type 1 
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 1'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
					annual_cost := annualized_capital_cost + operation_maintenance_cost
					annualized_capital_cost := capital_cost * cap_recovery_factor
					capital_cost := capital_cost_multiplier * design_capacity * scaling_factor * 1000
					operation_maintenance_cost := coalesce(fixed_operation_maintenance_cost, 0) + coalesce(variable_operation_maintenance_cost, 0)
					fixed_operation_maintenance_cost := fixed_om_cost_multiplier * design_capacity * 1000;
					variable_operation_maintenance_cost := variable_om_cost_multiplier * design_capacity * capacity_factor * 8760;
						scaling_factor := 
							case 
								when (measure_abbreviation = ''NSCR_UBCW'' or measure_abbreviation = ''NSCR_UBCT'') and design_capacity >= 600.0 then 1.0
								when design_capacity >= 500.0 then 1.0
								else scaling_factor_model_size ^ scaling_factor_exponent
							end;
						IF coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 THEN 
						     cap_recovery_factor := public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life);
						END IF;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(' || control_measure_equation_table_alias || '.value1/*capital_cost_multiplier*/ * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) 
					* (case 
						when (' || control_measure_table_alias || '.abbreviation = ''NSCR_UBCW'' or ' || control_measure_table_alias || '.abbreviation = ''NSCR_UBCT'') and public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) >= 600.0 then 1.0
						when public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) >= 500.0 then 1.0
						else ' || control_measure_equation_table_alias || '.value4/*scaling_factor_model_size*/ ^ ' || control_measure_equation_table_alias || '.value5/*scaling_factor_exponent*/
					end) /*scaling_factor*/
					* 1000) /*capital_cost*/
					)

				--Equation Type 2 
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 2'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 and coalesce(3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')), 0) <= 2000.0 then '
					/*
						-- calculate capital cost
						capital_cost := capital_cost_multiplier * (design_capacity ^ capital_cost_exponent);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := annual_cost_multiplier * design_capacity ^ annual_cost_exponent;

						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(case when coalesce(' || inv_table_alias || '.ceff, 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value1 else ' || control_measure_equation_table_alias || '.value5 end)/*capital_cost_multiplier*/ * ((3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, ''''))/*design_capacity*/) ^ (case when coalesce(' || inv_table_alias || '.ceff, 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value2 else ' || control_measure_equation_table_alias || '.value6 end)/*capital_cost_exponent*/)
					)

				--Equation Type 3 
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 3'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*

						cap_recovery_factor double precision := capital_recovery_factor;
						capital_cost_factor double precision := 192;
						gas_flow_rate_factor double precision := 0.486;
						retrofit_factor double precision := 1.1;

						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(1028000/ stack_flow_rate) ^ 0.6 * capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
								else
									capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
							end * 0.9383;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- 	Fixed O&M = Gas Flow Rate Factor * Fixed O&M Rate
						-- 		where Gas Flow Rate Factor = 0.486 kW/acfm and Fixed O&M Rate = $6.9/kW-yr.
						fixed_operation_maintenance_cost := 0.486 * 6.9 * stack_flow_rate;

						-- 	Variable O&M = Gas Flow Rate Factor * Variable O&M Rate * Hours per Year * STKFLOW * 60
						-- 		where Gas Flow Rate Factor = 0.486 kW/acf; Variable O&M Rate = $0.0015/kWh; 
						--		Hours per Year = 8,736 hours, STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						variable_operation_maintenance_cost := 0.486 * 0.0015 * 8736 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (3.35 + (0.000729 * 8736)) * stack_flow_rate * 0.9383; (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;

					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(
					case 
						when (' || inv_table_alias || '.stkflow * 60.0) < 1028000 then (1028000/ (' || inv_table_alias || '.stkflow * 60.0)) ^ 0.6
						else 1.0
					end * 192/*capital_cost_factor*/ * 0.486/*gas_flow_rate_factor*/ * 1.1/*retrofit_factor*/ * (' || inv_table_alias || '.stkflow * 60.0) * 0.9383)/*capital_cost*/
					)

				--Equation Type 4
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 4'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (990000 + 9.836 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $75,800
						--		where $75,800 is the fixed O&M cost based on model plant data
						fixed_operation_maintenance_cost := 75800;
						
						--	Variable O&M = $12.82 * STKFLOW * 60
						--		where $12.82 is the variable O&M cost based on model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 12.82 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (75800 + 12.82 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(990000 + 9.836 * (' || inv_table_alias || '.stkflow * 60.0))/*capital_cost*/ 
					)

				--Equation Type 5
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 5'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (2882540 + 244.74 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $749,170
						--		where $749,170 is the fixed O&M cost based on model plant data,
						fixed_operation_maintenance_cost := 749170;

						--	Variable O&M=$148.4 * STKFLOW * 60
						--		where $148.4 is the variable O&M data based on model plant data and credit for recovered product, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 148.40 * stack_flow_rate;
						
						-- 	calculate operation maintenance cost
						--	operation_maintenance_cost := (749170 + 148.40 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(2882540 + 244.74 * (' || inv_table_alias || '.stkflow * 60.0))/*capital_cost*/ 
					)

				--Equation Type 6
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 6'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (3449803 + 135.86 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $797,667
						--		where $797,667 is the fixed O&M cost derived from model plant data
						fixed_operation_maintenance_cost := 797667;

						--	Variable O&M = $58.84 * STKFLOW * 60
						--		where $58.84 is the variable O&M cost derived from model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 58.84 * stack_flow_rate;

						-- calculate operation maintenance cost
						--	operation_maintenance_cost := (797667 + 58.84 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(3449803 + 135.86 * (' || inv_table_alias || '.stkflow * 60.0))/*capital_cost*/ 
					)

				--Equation Type 7
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 7'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(2882540 + (244.74 * stack_flow_rate) + (((1028000 / stack_flow_rate) ^ 0.6)) * 93.3 * 1.1 * stack_flow_rate * 0.9383)
								else
									(2882540 + (244.74 * stack_flow_rate) + 93.3 * 1.1 * stack_flow_rate * 0.9383)
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate operation maintenance cost
						operation_maintenance_cost := (749170 + (148.40 * stack_flow_rate) + (3.35 + (0.000729 * 8736 ) * (stack_flow_rate) ^ 0.9383));

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					case 
						when (' || inv_table_alias || '.stkflow * 60.0) < 1028000 then 
							(2882540 + (244.74 * (' || inv_table_alias || '.stkflow * 60.0)) + (((1028000 / (' || inv_table_alias || '.stkflow * 60.0)) ^ 0.6)) * 93.3 * 1.1 * (' || inv_table_alias || '.stkflow * 60.0) * 0.9383)
						else
							(2882540 + (244.74 * (' || inv_table_alias || '.stkflow * 60.0)) + 93.3 * 1.1 * (' || inv_table_alias || '.stkflow * 60.0) * 0.9383)
					end/*capital_cost*/ 
					)

				--Equation Type 8
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 8'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then capital_control_cost_factor * stack_flow_rate
								else default_capital_cpt_factor * emis_reduction
							end;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then om_control_cost_factor * stack_flow_rate
								else default_om_cpt_factor * emis_reduction
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then annualized_capital_cost + 0.04 * capital_cost + operation_maintenance_cost
								else default_annualized_cpt_factor * emis_reduction
							end;

					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					case 
						when (' || inv_table_alias || '.stkflow * 60.0) >= 5.0 then ' || control_measure_equation_table_alias || '.value1/*capital_control_cost_factor*/ * (' || inv_table_alias || '.stkflow * 60.0)
						else ' || control_measure_equation_table_alias || '.value3/*default_capital_cpt_factor*/ * ' || emis_reduction_sql || '
					end 
					)

				-- Equation Type 9
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 9'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := ((total_equipment_cost_factor * stack_flow_rate) + total_equipment_cost_constant) * equipment_to_capital_cost_multiplier;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							((electricity_factor * stack_flow_rate) + electricity_constant) + ((dust_disposal_factor * stack_flow_rate) + dust_disposal_constant) + ((bag_replacement_factor * stack_flow_rate) + bag_replacement_constant);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(
						(
							(
								(' || control_measure_equation_table_alias || '.value1/*total_equipment_cost_factor*/ 
									* (' || inv_table_alias || '.stkflow * 60.0)
								) 
								+ ' || control_measure_equation_table_alias || '.value2/*total_equipment_cost_constant*/
							) * ' || control_measure_equation_table_alias || '.value3/*equipment_to_capital_cost_multiplier*/
						)
					)/*capital_cost*/
					)

				-- Equation Type 10
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 10'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := design_capacity * capital_cost_multiplier * 1000 * (250.0 / design_capacity) ^ capital_cost_exponent;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;


						-- calculate variable_operation_maintenance_cost 
						variable_operation_maintenance_cost := variable_operation_maintenance_cost_multiplier * design_capacity * 0.85 * annual_avg_hours_per_year;
						
						-- calculate fixed_operation_maintenance_cost
						fixed_operation_maintenance_cost := design_capacity * 1000 * fixed_operation_maintenance_cost_multiplier * (250 / design_capacity) ^ fixed_operation_maintenance_cost_exponent;

					--O2 *1000*0.24*(250/O2)^0.3

						-- calculate operation maintenance cost
						operation_maintenance_cost := variable_operation_maintenance_cost + fixed_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, 
							case 
								when length(coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, '''')) = 0 then 
									''MW''::character varying
								else
									' || inv_table_alias || '.design_capacity_unit_numerator
							end
							, coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) * ' || control_measure_equation_table_alias || '.value1/*capital_cost_multiplier*/ * 1000 * (250.0 / public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, 
							case 
								when length(coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, '''')) = 0 then 
									''MW''::character varying
								else
									' || inv_table_alias || '.design_capacity_unit_numerator
							end
							, coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, ''''))) ^ ' || control_measure_equation_table_alias || '.value2/*capital_cost_exponent*/)/*capital_cost*/
					)

				-- Equation Type 11
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 11'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
						-- figure out cost per ton
						computed_cost_per_ton := 
							case 
								when design_capacity <= low_boiler_capacity_range then low_default_cost_per_ton
								when design_capacity > low_boiler_capacity_range and design_capacity < medium_boiler_capacity_range then medium_default_cost_per_ton
								when design_capacity >= medium_boiler_capacity_range then high_default_cost_per_ton
							end;

						-- calculate annual cost
						annual_cost := emis_reduction * computed_cost_per_ton;
						-- calculate capital cost
						capital_cost := annual_cost  * capital_annualized_ratio;
						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;
						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					' || emis_reduction_sql || ' 
					* (case 
						when 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) <= ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value1/*low_default_cost_per_ton*/
						when 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) > ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ and 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) < ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value3/*medium_default_cost_per_ton*/
						when 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) >= ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value5/*high_default_cost_per_ton*/
					end)/*computed_cost_per_ton*/ * ' || control_measure_efficiencyrecord_table_alias || '.cap_ann_ratio::double precision
					)

				--Equation Type 12
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 12'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 and coalesce(160.0/*stktemp*/, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (coalesce(total_capital_investment_fixed_factor, 0.0) + coalesce(total_capital_investment_variable_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000) ^ 0.6;

						-- calculate fixed operation maintenance cost
						fixed_operation_maintenance_cost := (coalesce(annual_operating_cost_fixed_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate variable operation maintenance cost
						variable_operation_maintenance_cost := (coalesce(annual_operating_cost_variable_factor,0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate operation maintenance cost
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := operation_maintenance_cost + annualized_capital_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(
					coalesce(' || control_measure_equation_table_alias || '.value1/*total_capital_investment_fixed_factor*/, 0.0) 
					+ coalesce(' || control_measure_equation_table_alias || '.value2/*total_capital_investment_variable_factor*/, 0.0)
					) 
					* ((' || inv_table_alias || '.stkflow * 60.0/*stack_flow_rate*/ * 520 / (160.0/*stktemp*//*stack_temperature*/ + 460.0)) / 150000) ^ 0.6
					)

				else 
			' else '
		' end || '
					/*
						-- calculate annual cost
						annual_cost := emis_reduction * ref_yr_cost_per_ton;
						-- calculate capital cost
						capital_cost := annual_cost  * capital_annualized_ratio;
						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;
						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - coalesce(annualized_capital_cost, 0);
					*/
					' || emis_reduction_sql || ' * ' || control_measure_efficiencyrecord_table_alias || '.ref_yr_cost_per_ton * ' || control_measure_efficiencyrecord_table_alias || '.cap_ann_ratio::double precision
		' || case 
			when use_cost_equations then '
			end 
			' else '
		' end || ')';

	-- prepare operation_maintenance_cost_expression 
	operation_maintenance_cost_expression := 
	case 
		when use_cost_equations then 
			'(case 

				--Equation Type 1 
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 1'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
					annual_cost := annualized_capital_cost + operation_maintenance_cost
					annualized_capital_cost := capital_cost * cap_recovery_factor
					capital_cost := capital_cost_multiplier * design_capacity * scaling_factor * 1000
					operation_maintenance_cost := coalesce(fixed_operation_maintenance_cost, 0) + coalesce(variable_operation_maintenance_cost, 0)
					fixed_operation_maintenance_cost := fixed_om_cost_multiplier * design_capacity * 1000;
					variable_operation_maintenance_cost := variable_om_cost_multiplier * design_capacity * capacity_factor * 8760;
						scaling_factor := 
							case 
								when (measure_abbreviation = ''NSCR_UBCW'' or measure_abbreviation = ''NSCR_UBCT'') and design_capacity >= 600.0 then 1.0
								when design_capacity >= 500.0 then 1.0
								else scaling_factor_model_size ^ scaling_factor_exponent
							end;
						IF coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 THEN 
						     cap_recovery_factor := public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life);
						END IF;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					/*operation_maintenance_cost*/
					coalesce(' || control_measure_equation_table_alias || '.value2/*fixed_om_cost_multiplier*/ * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) * 1000/*fixed_operation_maintenance_cost*/, 0) 
					+ coalesce(' || control_measure_equation_table_alias || '.value3/*variable_om_cost_multiplier*/ * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) * ' || control_measure_equation_table_alias || '.value6/*capacity_factor*/ * 8760/*variable_operation_maintenance_cost*/, 0)
					)

				--Equation Type 2 
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 2'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 and coalesce(3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')), 0) <= 2000.0 then '
					/*
						-- calculate capital cost
						capital_cost := capital_cost_multiplier * (design_capacity ^ capital_cost_exponent);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := annual_cost_multiplier * design_capacity ^ annual_cost_exponent;

						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(case when coalesce(' || inv_table_alias || '.ceff, 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value3 else ' || control_measure_equation_table_alias || '.value7 end)/*annual_cost_multiplier*/ * ((3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, ''''))/*design_capacity*/) ^ (case when coalesce(' || inv_table_alias || '.ceff, 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value4 else ' || control_measure_equation_table_alias || '.value8 end)/*annual_cost_exponent*/)
					- (
						(case when coalesce(' || inv_table_alias || '.ceff, 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value1 else ' || control_measure_equation_table_alias || '.value5 end)/*capital_cost_multiplier*/ * ((3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, ''''))/*design_capacity*/) ^ (case when coalesce(' || inv_table_alias || '.ceff, 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value2 else ' || control_measure_equation_table_alias || '.value6 end)/*capital_cost_exponent*/)/*capital_cost*/
						* (case 
							when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
							else cap_rec_factor
						end)/*cap_recovery_factor*//*annualized_capital_cost*/
					)
					)
					
				--Equation Type 3 
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 3'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*

						cap_recovery_factor double precision := capital_recovery_factor;
						capital_cost_factor double precision := 192;
						gas_flow_rate_factor double precision := 0.486;
						retrofit_factor double precision := 1.1;

						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(1028000/ stack_flow_rate) ^ 0.6 * capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
								else
									capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
							end * 0.9383;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- 	Fixed O&M = Gas Flow Rate Factor * Fixed O&M Rate
						-- 		where Gas Flow Rate Factor = 0.486 kW/acfm and Fixed O&M Rate = $6.9/kW-yr.
						fixed_operation_maintenance_cost := 0.486 * 6.9 * stack_flow_rate;

						-- 	Variable O&M = Gas Flow Rate Factor * Variable O&M Rate * Hours per Year * STKFLOW * 60
						-- 		where Gas Flow Rate Factor = 0.486 kW/acf; Variable O&M Rate = $0.0015/kWh; 
						--		Hours per Year = 8,736 hours, STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						variable_operation_maintenance_cost := 0.486 * 0.0015 * 8736 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (3.35 + (0.000729 * 8736)) * stack_flow_rate * 0.9383; (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;

					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(
						(0.486 * 6.9 * (' || inv_table_alias || '.stkflow * 60.0))/*fixed_operation_maintenance_cost*/
						+ 0.486 * 0.0015 * 8736 * (' || inv_table_alias || '.stkflow * 60.0)/*variable_operation_maintenance_cost*/
					)/*operation_maintenance_cost*/
					)

				--Equation Type 4
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 4'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (990000 + 9.836 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $75,800
						--		where $75,800 is the fixed O&M cost based on model plant data
						fixed_operation_maintenance_cost := 75800;
						
						--	Variable O&M = $12.82 * STKFLOW * 60
						--		where $12.82 is the variable O&M cost based on model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 12.82 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (75800 + 12.82 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(
					75800/*fixed_operation_maintenance_cost*/ + (12.82 * (' || inv_table_alias || '.stkflow * 60.0))/*variable_operation_maintenance_cost*/
					)/*operation_maintenance_cost*/
					)

				--Equation Type 5
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 5'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (2882540 + 244.74 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $749,170
						--		where $749,170 is the fixed O&M cost based on model plant data,
						fixed_operation_maintenance_cost := 749170;

						--	Variable O&M=$148.4 * STKFLOW * 60
						--		where $148.4 is the variable O&M data based on model plant data and credit for recovered product, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 148.40 * stack_flow_rate;
						
						-- 	calculate operation maintenance cost
						--	operation_maintenance_cost := (749170 + 148.40 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(
					749170/*fixed_operation_maintenance_cost*/ + (148.40 * (' || inv_table_alias || '.stkflow * 60.0))/*variable_operation_maintenance_cost*/
					)/*operation_maintenance_cost*/
					)

				--Equation Type 6
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 6'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (3449803 + 135.86 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $797,667
						--		where $797,667 is the fixed O&M cost derived from model plant data
						fixed_operation_maintenance_cost := 797667;

						--	Variable O&M = $58.84 * STKFLOW * 60
						--		where $58.84 is the variable O&M cost derived from model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 58.84 * stack_flow_rate;

						-- calculate operation maintenance cost
						--	operation_maintenance_cost := (797667 + 58.84 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(
					797667/*fixed_operation_maintenance_cost*/ + (58.84 * (' || inv_table_alias || '.stkflow * 60.0))/*variable_operation_maintenance_cost*/
					)/*operation_maintenance_cost*/
					)

				--Equation Type 7
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 7'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(2882540 + (244.74 * stack_flow_rate) + (((1028000 / stack_flow_rate) ^ 0.6)) * 93.3 * 1.1 * stack_flow_rate * 0.9383)
								else
									(2882540 + (244.74 * stack_flow_rate) + 93.3 * 1.1 * stack_flow_rate * 0.9383)
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate operation maintenance cost
						operation_maintenance_cost := (749170 + (148.40 * stack_flow_rate) + (3.35 + (0.000729 * 8736 ) * (stack_flow_rate) ^ 0.9383));

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(749170 + (148.40 * (' || inv_table_alias || '.stkflow * 60.0)) + (3.35 + (0.000729 * 8736 ) * ((' || inv_table_alias || '.stkflow * 60.0)) ^ 0.9383))/*operation_maintenance_cost*/
					)

				--Equation Type 8
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 8'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then capital_control_cost_factor * stack_flow_rate
								else default_capital_cpt_factor * emis_reduction
							end;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then om_control_cost_factor * stack_flow_rate
								else default_om_cpt_factor * emis_reduction
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then annualized_capital_cost + 0.04 * capital_cost + operation_maintenance_cost
								else default_annualized_cpt_factor * emis_reduction
							end;

					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					case 
						when (' || inv_table_alias || '.stkflow * 60.0) >= 5.0 then ' || control_measure_equation_table_alias || '.value2/*om_control_cost_factor*/ * (' || inv_table_alias || '.stkflow * 60.0)
						else ' || control_measure_equation_table_alias || '.value4/*default_om_cpt_factor*/ * ' || emis_reduction_sql || '
					end/*operation_maintenance_cost*/
					)

				-- Equation Type 9
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 9'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := ((total_equipment_cost_factor * stack_flow_rate) + total_equipment_cost_constant) * equipment_to_capital_cost_multiplier;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							((electricity_factor * stack_flow_rate) + electricity_constant) + ((dust_disposal_factor * stack_flow_rate) + dust_disposal_constant) + ((bag_replacement_factor * stack_flow_rate) + bag_replacement_constant);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(((' || control_measure_equation_table_alias || '.value4/*electricity_factor*/ * (' || inv_table_alias || '.stkflow * 60.0)) + ' || control_measure_equation_table_alias || '.value5/*electricity_constant*/) + ((' || control_measure_equation_table_alias || '.value6/*dust_disposal_factor*/ * (' || inv_table_alias || '.stkflow * 60.0)) + ' || control_measure_equation_table_alias || '.value7/*dust_disposal_constant*/) + ((' || control_measure_equation_table_alias || '.value8/*bag_replacement_factor*/ * (' || inv_table_alias || '.stkflow * 60.0)) + ' || control_measure_equation_table_alias || '.value9/*bag_replacement_constant*/))/*operation_maintenance_cost*/
					)

				-- Equation Type 10
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 10'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := design_capacity * capital_cost_multiplier * 1000 * (250.0 / design_capacity) ^ capital_cost_exponent;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;


						-- calculate variable_operation_maintenance_cost 
						variable_operation_maintenance_cost := variable_operation_maintenance_cost_multiplier * design_capacity * 0.85 * annual_avg_hours_per_year;
						
						-- calculate fixed_operation_maintenance_cost
						fixed_operation_maintenance_cost := design_capacity * 1000 * fixed_operation_maintenance_cost_multiplier * (250 / design_capacity) ^ fixed_operation_maintenance_cost_exponent;

					--O2 *1000*0.24*(250/O2)^0.3

						-- calculate operation maintenance cost
						operation_maintenance_cost := variable_operation_maintenance_cost + fixed_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					((' || control_measure_equation_table_alias || '.value3/*variable_operation_maintenance_cost_multiplier*/ * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, 
							case 
								when length(coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, '''')) = 0 then 
									''MW''::character varying
								else
									' || inv_table_alias || '.design_capacity_unit_numerator
							end
							, coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) * 0.85 * ' || inv_table_alias || '.annual_avg_hours_per_year)/*variable_operation_maintenance_cost*/ + (public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, 
							case 
								when length(coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, '''')) = 0 then 
									''MW''::character varying
								else
									' || inv_table_alias || '.design_capacity_unit_numerator
							end
							, coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) * 1000 * ' || control_measure_equation_table_alias || '.value4/*fixed_operation_maintenance_cost_multiplier*/ * (250 / public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, 
							case 
								when length(coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, '''')) = 0 then 
									''MW''::character varying
								else
									' || inv_table_alias || '.design_capacity_unit_numerator
							end
							, coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, ''''))) ^ ' || control_measure_equation_table_alias || '.value5/*fixed_operation_maintenance_cost_exponent*/)/*fixed_operation_maintenance_cost*/)/*operation_maintenance_cost*/
					)

				-- Equation Type 11
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 11'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
						-- figure out cost per ton
						computed_cost_per_ton := 
							case 
								when design_capacity <= low_boiler_capacity_range then low_default_cost_per_ton
								when design_capacity > low_boiler_capacity_range and design_capacity < medium_boiler_capacity_range then medium_default_cost_per_ton
								when design_capacity >= medium_boiler_capacity_range then high_default_cost_per_ton
							end;

						-- calculate annual cost
						annual_cost := emis_reduction * computed_cost_per_ton;
						-- calculate capital cost
						capital_cost := annual_cost  * capital_annualized_ratio;
						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;
						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					' || emis_reduction_sql || ' 
					* (case 
						when 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) <= ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value1/*low_default_cost_per_ton*/
						when 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) > ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ and 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) < ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value3/*medium_default_cost_per_ton*/
						when 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) >= ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value5/*high_default_cost_per_ton*/
					end)/*computed_cost_per_ton*//*annual_cost*/
					- ' || emis_reduction_sql || ' 
					* (case 
						when 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) <= ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value1/*low_default_cost_per_ton*/
						when 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) > ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ and 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) < ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value3/*medium_default_cost_per_ton*/
						when 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) >= ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value5/*high_default_cost_per_ton*/
					end)/*computed_cost_per_ton*/ * ' || control_measure_efficiencyrecord_table_alias || '.cap_ann_ratio::double precision
					* (case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end)/*cap_recovery_factor*//*annualized_capital_cost*/
					)
					
				-- Equation Type 12
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 12'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 and coalesce(160.0/*stktemp*/, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (coalesce(total_capital_investment_fixed_factor, 0.0) + coalesce(total_capital_investment_variable_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000) ^ 0.6;

						-- calculate fixed operation maintenance cost
						fixed_operation_maintenance_cost := (coalesce(annual_operating_cost_fixed_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate variable operation maintenance cost
						variable_operation_maintenance_cost := (coalesce(annual_operating_cost_variable_factor,0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate operation maintenance cost
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := operation_maintenance_cost + annualized_capital_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(coalesce(' || control_measure_equation_table_alias || '.value3/*annual_operating_cost_fixed_factor*/, 0.0)) * ((' || inv_table_alias || '.stkflow * 60.0/*stack_flow_rate*/ * 520 / (160.0/*stktemp*//*stack_temperature*/ + 460.0)) / 150000)
					+ (coalesce(' || control_measure_equation_table_alias || '.value4/*annual_operating_cost_variable_factor*/, 0.0)) * ((' || inv_table_alias || '.stkflow * 60.0/*stack_flow_rate*/ * 520 / (160.0/*stktemp*//*stack_temperature*/ + 460.0)) / 150000)
					)
				-- Default Approach
				else 
			' else '
		' end || '
					/*
						-- calculate annual cost
						annual_cost := emis_reduction * ref_yr_cost_per_ton;
						-- calculate capital cost
						capital_cost := annual_cost  * capital_annualized_ratio;
						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;
						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - coalesce(annualized_capital_cost, 0);
					*/
					' || emis_reduction_sql || ' * ' || control_measure_efficiencyrecord_table_alias || '.ref_yr_cost_per_ton
					 - coalesce(' || emis_reduction_sql || ' * ' || control_measure_efficiencyrecord_table_alias || '.ref_yr_cost_per_ton * ' || control_measure_efficiencyrecord_table_alias || '.cap_ann_ratio::double precision * case 
								when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
								else cap_rec_factor
							end/*cap_recovery_factor*/, 0)
		' || case 
			when use_cost_equations then '
			end 
			' else '
		' end || ')';

	-- prepare fixed_operation_maintenance_cost_expression 
	fixed_operation_maintenance_cost_expression := 
	case 
		when use_cost_equations then 
			'(case 

				--Equation Type 1 
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 1'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
					annual_cost := annualized_capital_cost + operation_maintenance_cost
					annualized_capital_cost := capital_cost * cap_recovery_factor
					capital_cost := capital_cost_multiplier * design_capacity * scaling_factor * 1000
					operation_maintenance_cost := coalesce(fixed_operation_maintenance_cost, 0) + coalesce(variable_operation_maintenance_cost, 0)
					fixed_operation_maintenance_cost := fixed_om_cost_multiplier * design_capacity * 1000;
					variable_operation_maintenance_cost := variable_om_cost_multiplier * design_capacity * capacity_factor * 8760;
						scaling_factor := 
							case 
								when (measure_abbreviation = ''NSCR_UBCW'' or measure_abbreviation = ''NSCR_UBCT'') and design_capacity >= 600.0 then 1.0
								when design_capacity >= 500.0 then 1.0
								else scaling_factor_model_size ^ scaling_factor_exponent
							end;
						IF coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 THEN 
						     cap_recovery_factor := public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life);
						END IF;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					' || control_measure_equation_table_alias || '.value2/*fixed_om_cost_multiplier*/ * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) * 1000/*fixed_operation_maintenance_cost*/
					)

				--Equation Type 2 
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 2'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 and coalesce(3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')), 0) <= 2000.0 then '
					/*
						-- calculate capital cost
						capital_cost := capital_cost_multiplier * (design_capacity ^ capital_cost_exponent);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := annual_cost_multiplier * design_capacity ^ annual_cost_exponent;

						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					null::double precision
					
				--Equation Type 3 
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 3'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*

						cap_recovery_factor double precision := capital_recovery_factor;
						capital_cost_factor double precision := 192;
						gas_flow_rate_factor double precision := 0.486;
						retrofit_factor double precision := 1.1;

						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(1028000/ stack_flow_rate) ^ 0.6 * capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
								else
									capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
							end * 0.9383;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- 	Fixed O&M = Gas Flow Rate Factor * Fixed O&M Rate
						-- 		where Gas Flow Rate Factor = 0.486 kW/acfm and Fixed O&M Rate = $6.9/kW-yr.
						fixed_operation_maintenance_cost := 0.486 * 6.9 * stack_flow_rate;

						-- 	Variable O&M = Gas Flow Rate Factor * Variable O&M Rate * Hours per Year * STKFLOW * 60
						-- 		where Gas Flow Rate Factor = 0.486 kW/acf; Variable O&M Rate = $0.0015/kWh; 
						--		Hours per Year = 8,736 hours, STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						variable_operation_maintenance_cost := 0.486 * 0.0015 * 8736 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (3.35 + (0.000729 * 8736)) * stack_flow_rate * 0.9383; (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;

					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(0.486 * 6.9 * (' || inv_table_alias || '.stkflow * 60.0))/*fixed_operation_maintenance_cost*/
					)

				--Equation Type 4
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 4'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (990000 + 9.836 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $75,800
						--		where $75,800 is the fixed O&M cost based on model plant data
						fixed_operation_maintenance_cost := 75800;
						
						--	Variable O&M = $12.82 * STKFLOW * 60
						--		where $12.82 is the variable O&M cost based on model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 12.82 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (75800 + 12.82 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					75800/*fixed_operation_maintenance_cost*/

				--Equation Type 5
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 5'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (2882540 + 244.74 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $749,170
						--		where $749,170 is the fixed O&M cost based on model plant data,
						fixed_operation_maintenance_cost := 749170;

						--	Variable O&M=$148.4 * STKFLOW * 60
						--		where $148.4 is the variable O&M data based on model plant data and credit for recovered product, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 148.40 * stack_flow_rate;
						
						-- 	calculate operation maintenance cost
						--	operation_maintenance_cost := (749170 + 148.40 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					749170.0/*fixed_operation_maintenance_cost*/

				--Equation Type 6
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 6'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (3449803 + 135.86 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $797,667
						--		where $797,667 is the fixed O&M cost derived from model plant data
						fixed_operation_maintenance_cost := 797667;

						--	Variable O&M = $58.84 * STKFLOW * 60
						--		where $58.84 is the variable O&M cost derived from model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 58.84 * stack_flow_rate;

						-- calculate operation maintenance cost
						--	operation_maintenance_cost := (797667 + 58.84 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					797667.0/*fixed_operation_maintenance_cost*/

				--Equation Type 7
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 7'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(2882540 + (244.74 * stack_flow_rate) + (((1028000 / stack_flow_rate) ^ 0.6)) * 93.3 * 1.1 * stack_flow_rate * 0.9383)
								else
									(2882540 + (244.74 * stack_flow_rate) + 93.3 * 1.1 * stack_flow_rate * 0.9383)
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate operation maintenance cost
						operation_maintenance_cost := (749170 + (148.40 * stack_flow_rate) + (3.35 + (0.000729 * 8736 ) * (stack_flow_rate) ^ 0.9383));

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					null::double precision

				--Equation Type 8
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 8'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then capital_control_cost_factor * stack_flow_rate
								else default_capital_cpt_factor * emis_reduction
							end;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then om_control_cost_factor * stack_flow_rate
								else default_om_cpt_factor * emis_reduction
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then annualized_capital_cost + 0.04 * capital_cost + operation_maintenance_cost
								else default_annualized_cpt_factor * emis_reduction
							end;

					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					null::double precision

				-- Equation Type 9
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 9'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := ((total_equipment_cost_factor * stack_flow_rate) + total_equipment_cost_constant) * equipment_to_capital_cost_multiplier;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							((electricity_factor * stack_flow_rate) + electricity_constant) + ((dust_disposal_factor * stack_flow_rate) + dust_disposal_constant) + ((bag_replacement_factor * stack_flow_rate) + bag_replacement_constant);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					null::double precision

				-- Equation Type 10
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 10'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := design_capacity * capital_cost_multiplier * 1000 * (250.0 / design_capacity) ^ capital_cost_exponent;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;


						-- calculate variable_operation_maintenance_cost 
						variable_operation_maintenance_cost := variable_operation_maintenance_cost_multiplier * design_capacity * 0.85 * annual_avg_hours_per_year;
						
						-- calculate fixed_operation_maintenance_cost
						fixed_operation_maintenance_cost := design_capacity * 1000 * fixed_operation_maintenance_cost_multiplier * (250 / design_capacity) ^ fixed_operation_maintenance_cost_exponent;

					--O2 *1000*0.24*(250/O2)^0.3

						-- calculate operation maintenance cost
						operation_maintenance_cost := variable_operation_maintenance_cost + fixed_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, 
						case 
							when length(coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, '''')) = 0 then 
								''MW''::character varying
							else
								' || inv_table_alias || '.design_capacity_unit_numerator
						end
						, coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) * 1000 * ' || control_measure_equation_table_alias || '.value4/*fixed_operation_maintenance_cost_multiplier*/ * (250 / public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, 
						case 
							when length(coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, '''')) = 0 then 
								''MW''::character varying
							else
								' || inv_table_alias || '.design_capacity_unit_numerator
						end
						, coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, ''''))) ^ ' || control_measure_equation_table_alias || '.value5/*fixed_operation_maintenance_cost_exponent*/)/*fixed_operation_maintenance_cost*/
					)

				-- Equation Type 11
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 11'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
						-- figure out cost per ton
						computed_cost_per_ton := 
							case 
								when design_capacity <= low_boiler_capacity_range then low_default_cost_per_ton
								when design_capacity > low_boiler_capacity_range and design_capacity < medium_boiler_capacity_range then medium_default_cost_per_ton
								when design_capacity >= medium_boiler_capacity_range then high_default_cost_per_ton
							end;

						-- calculate annual cost
						annual_cost := emis_reduction * computed_cost_per_ton;
						-- calculate capital cost
						capital_cost := annual_cost  * capital_annualized_ratio;
						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;
						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					null::double precision
					
				-- Equation Type 12
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 12'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 and coalesce(160.0/*stktemp*/, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (coalesce(total_capital_investment_fixed_factor, 0.0) + coalesce(total_capital_investment_variable_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000) ^ 0.6;

						-- calculate fixed operation maintenance cost
						fixed_operation_maintenance_cost := (coalesce(annual_operating_cost_fixed_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate variable operation maintenance cost
						variable_operation_maintenance_cost := (coalesce(annual_operating_cost_variable_factor,0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate operation maintenance cost
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := operation_maintenance_cost + annualized_capital_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(coalesce(' || control_measure_equation_table_alias || '.value3/*annual_operating_cost_fixed_factor*/, 0.0)) * ((' || inv_table_alias || '.stkflow * 60.0/*stack_flow_rate*/ * 520 / (160.0/*stktemp*//*stack_temperature*/ + 460.0)) / 150000)
					)
				else 
			' else '
		' end || '
					null::double precision
		' || case 
			when use_cost_equations then '
			end 
			' else '
		' end || ')';

	-- prepare variable_operation_maintenance_cost_expression 
	variable_operation_maintenance_cost_expression := 
	case 
		when use_cost_equations then 
			'(case 

				--Equation Type 1 
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 1'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
					annual_cost := annualized_capital_cost + operation_maintenance_cost
					annualized_capital_cost := capital_cost * cap_recovery_factor
					capital_cost := capital_cost_multiplier * design_capacity * scaling_factor * 1000
					operation_maintenance_cost := coalesce(fixed_operation_maintenance_cost, 0) + coalesce(variable_operation_maintenance_cost, 0)
					fixed_operation_maintenance_cost := fixed_om_cost_multiplier * design_capacity * 1000;
					variable_operation_maintenance_cost := variable_om_cost_multiplier * design_capacity * capacity_factor * 8760;
						scaling_factor := 
							case 
								when (measure_abbreviation = ''NSCR_UBCW'' or measure_abbreviation = ''NSCR_UBCT'') and design_capacity >= 600.0 then 1.0
								when design_capacity >= 500.0 then 1.0
								else scaling_factor_model_size ^ scaling_factor_exponent
							end;
						IF coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 THEN 
						     cap_recovery_factor := public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life);
						END IF;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					' || control_measure_equation_table_alias || '.value3/*variable_om_cost_multiplier*/ * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) * ' || control_measure_equation_table_alias || '.value6/*capacity_factor*/ * 8760/*variable_operation_maintenance_cost*/
					)

				--Equation Type 2 
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 2'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 and coalesce(3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')), 0) <= 2000.0 then '
					/*
						-- calculate capital cost
						capital_cost := capital_cost_multiplier * (design_capacity ^ capital_cost_exponent);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := annual_cost_multiplier * design_capacity ^ annual_cost_exponent;

						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					null::double precision
					
				--Equation Type 3 
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 3'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*

						cap_recovery_factor double precision := capital_recovery_factor;
						capital_cost_factor double precision := 192;
						gas_flow_rate_factor double precision := 0.486;
						retrofit_factor double precision := 1.1;

						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(1028000/ stack_flow_rate) ^ 0.6 * capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
								else
									capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
							end * 0.9383;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- 	Fixed O&M = Gas Flow Rate Factor * Fixed O&M Rate
						-- 		where Gas Flow Rate Factor = 0.486 kW/acfm and Fixed O&M Rate = $6.9/kW-yr.
						fixed_operation_maintenance_cost := 0.486 * 6.9 * stack_flow_rate;

						-- 	Variable O&M = Gas Flow Rate Factor * Variable O&M Rate * Hours per Year * STKFLOW * 60
						-- 		where Gas Flow Rate Factor = 0.486 kW/acf; Variable O&M Rate = $0.0015/kWh; 
						--		Hours per Year = 8,736 hours, STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						variable_operation_maintenance_cost := 0.486 * 0.0015 * 8736 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (3.35 + (0.000729 * 8736)) * stack_flow_rate * 0.9383; (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;

					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					0.486 * 0.0015 * 8736 * (' || inv_table_alias || '.stkflow * 60.0)/*variable_operation_maintenance_cost*/
					)

				--Equation Type 4
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 4'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (990000 + 9.836 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $75,800
						--		where $75,800 is the fixed O&M cost based on model plant data
						fixed_operation_maintenance_cost := 75800;
						
						--	Variable O&M = $12.82 * STKFLOW * 60
						--		where $12.82 is the variable O&M cost based on model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 12.82 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (75800 + 12.82 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(12.82 * (' || inv_table_alias || '.stkflow * 60.0))/*variable_operation_maintenance_cost*/
					)

				--Equation Type 5
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 5'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (2882540 + 244.74 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $749,170
						--		where $749,170 is the fixed O&M cost based on model plant data,
						fixed_operation_maintenance_cost := 749170;

						--	Variable O&M=$148.4 * STKFLOW * 60
						--		where $148.4 is the variable O&M data based on model plant data and credit for recovered product, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 148.40 * stack_flow_rate;
						
						-- 	calculate operation maintenance cost
						--	operation_maintenance_cost := (749170 + 148.40 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(148.40 * (' || inv_table_alias || '.stkflow * 60.0))/*variable_operation_maintenance_cost*/
					)

				--Equation Type 6
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 6'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (3449803 + 135.86 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $797,667
						--		where $797,667 is the fixed O&M cost derived from model plant data
						fixed_operation_maintenance_cost := 797667;

						--	Variable O&M = $58.84 * STKFLOW * 60
						--		where $58.84 is the variable O&M cost derived from model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 58.84 * stack_flow_rate;

						-- calculate operation maintenance cost
						--	operation_maintenance_cost := (797667 + 58.84 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(58.84 * (' || inv_table_alias || '.stkflow * 60.0))/*variable_operation_maintenance_cost*/
					)

				--Equation Type 7
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 7'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(2882540 + (244.74 * stack_flow_rate) + (((1028000 / stack_flow_rate) ^ 0.6)) * 93.3 * 1.1 * stack_flow_rate * 0.9383)
								else
									(2882540 + (244.74 * stack_flow_rate) + 93.3 * 1.1 * stack_flow_rate * 0.9383)
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate operation maintenance cost
						operation_maintenance_cost := (749170 + (148.40 * stack_flow_rate) + (3.35 + (0.000729 * 8736 ) * (stack_flow_rate) ^ 0.9383));

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					null::double precision

				--Equation Type 8
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 8'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then capital_control_cost_factor * stack_flow_rate
								else default_capital_cpt_factor * emis_reduction
							end;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then om_control_cost_factor * stack_flow_rate
								else default_om_cpt_factor * emis_reduction
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then annualized_capital_cost + 0.04 * capital_cost + operation_maintenance_cost
								else default_annualized_cpt_factor * emis_reduction
							end;

					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					null::double precision

				-- Equation Type 9
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 9'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := ((total_equipment_cost_factor * stack_flow_rate) + total_equipment_cost_constant) * equipment_to_capital_cost_multiplier;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							((electricity_factor * stack_flow_rate) + electricity_constant) + ((dust_disposal_factor * stack_flow_rate) + dust_disposal_constant) + ((bag_replacement_factor * stack_flow_rate) + bag_replacement_constant);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					null::double precision

				-- Equation Type 10
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 10'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := design_capacity * capital_cost_multiplier * 1000 * (250.0 / design_capacity) ^ capital_cost_exponent;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;


						-- calculate variable_operation_maintenance_cost 
						variable_operation_maintenance_cost := variable_operation_maintenance_cost_multiplier * design_capacity * 0.85 * annual_avg_hours_per_year;
						
						-- calculate fixed_operation_maintenance_cost
						fixed_operation_maintenance_cost := design_capacity * 1000 * fixed_operation_maintenance_cost_multiplier * (250 / design_capacity) ^ fixed_operation_maintenance_cost_exponent;

					--O2 *1000*0.24*(250/O2)^0.3

						-- calculate operation maintenance cost
						operation_maintenance_cost := variable_operation_maintenance_cost + fixed_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					((' || control_measure_equation_table_alias || '.value3/*variable_operation_maintenance_cost_multiplier*/ * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, 
							case 
								when length(coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, '''')) = 0 then 
									''MW''::character varying
								else
									' || inv_table_alias || '.design_capacity_unit_numerator
							end
							, coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) * 0.85 * ' || inv_table_alias || '.annual_avg_hours_per_year))/*variable_operation_maintenance_cost*/
					)

				-- Equation Type 11
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 11'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
						-- figure out cost per ton
						computed_cost_per_ton := 
							case 
								when design_capacity <= low_boiler_capacity_range then low_default_cost_per_ton
								when design_capacity > low_boiler_capacity_range and design_capacity < medium_boiler_capacity_range then medium_default_cost_per_ton
								when design_capacity >= medium_boiler_capacity_range then high_default_cost_per_ton
							end;

						-- calculate annual cost
						annual_cost := emis_reduction * computed_cost_per_ton;
						-- calculate capital cost
						capital_cost := annual_cost  * capital_annualized_ratio;
						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;
						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					null::double precision
					
				-- Equation Type 12
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 12'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 and coalesce(160.0/*stktemp*/, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (coalesce(total_capital_investment_fixed_factor, 0.0) + coalesce(total_capital_investment_variable_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000) ^ 0.6;

						-- calculate fixed operation maintenance cost
						fixed_operation_maintenance_cost := (coalesce(annual_operating_cost_fixed_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate variable operation maintenance cost
						variable_operation_maintenance_cost := (coalesce(annual_operating_cost_variable_factor,0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate operation maintenance cost
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := operation_maintenance_cost + annualized_capital_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(coalesce(' || control_measure_equation_table_alias || '.value4/*annual_operating_cost_variable_factor*/, 0.0)) * ((' || inv_table_alias || '.stkflow * 60.0/*stack_flow_rate*/ * 520 / (160.0/*stktemp*//*stack_temperature*/ + 460.0)) / 150000)
					)
				else 
			' else '
		' end || '
					null::double precision
		' || case 
			when use_cost_equations then '
			end 
			' else '
		' end || ')';

	-- prepare annualized_capital_cost_expression 
	annualized_capital_cost_expression := 
	case 
		when use_cost_equations then 
			'(case 

				--Equation Type 1 
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 1'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
					annual_cost := annualized_capital_cost + operation_maintenance_cost
					annualized_capital_cost := capital_cost * cap_recovery_factor
					capital_cost := capital_cost_multiplier * design_capacity * scaling_factor * 1000
					operation_maintenance_cost := coalesce(fixed_operation_maintenance_cost, 0) + coalesce(variable_operation_maintenance_cost, 0)
					fixed_operation_maintenance_cost := fixed_om_cost_multiplier * design_capacity * 1000;
					variable_operation_maintenance_cost := variable_om_cost_multiplier * design_capacity * capacity_factor * 8760;
						scaling_factor := 
							case 
								when (measure_abbreviation = ''NSCR_UBCW'' or measure_abbreviation = ''NSCR_UBCT'') and design_capacity >= 600.0 then 1.0
								when design_capacity >= 500.0 then 1.0
								else scaling_factor_model_size ^ scaling_factor_exponent
							end;
						IF coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 THEN 
						     cap_recovery_factor := public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life);
						END IF;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(' || control_measure_equation_table_alias || '.value1/*capital_cost_multiplier*/ * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) 
					* (case 
						when (' || control_measure_table_alias || '.abbreviation = ''NSCR_UBCW'' or ' || control_measure_table_alias || '.abbreviation = ''NSCR_UBCT'') and public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) >= 600.0 then 1.0
						when public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) >= 500.0 then 1.0
						else ' || control_measure_equation_table_alias || '.value4/*scaling_factor_model_size*/ ^ ' || control_measure_equation_table_alias || '.value5/*scaling_factor_exponent*/
					end) /*scaling_factor*/
					* 1000) /*capital_cost*/
					* (case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end)/*cap_recovery_factor*/
					)
					
				--Equation Type 2 
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 2'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 and coalesce(3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')), 0) <= 2000.0 then '
					/*
						-- calculate capital cost
						capital_cost := capital_cost_multiplier * (design_capacity ^ capital_cost_exponent);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := annual_cost_multiplier * design_capacity ^ annual_cost_exponent;

						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(case when coalesce(' || inv_table_alias || '.ceff, 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value1 else ' || control_measure_equation_table_alias || '.value5 end)/*capital_cost_multiplier*/ * ((3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, ''''))/*design_capacity*/) ^ (case when coalesce(' || inv_table_alias || '.ceff, 0.0) = 0.0 then ' || control_measure_equation_table_alias || '.value2 else ' || control_measure_equation_table_alias || '.value6 end)/*capital_cost_exponent*/)
					* (case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end)/*cap_recovery_factor*/
					)

				--Equation Type 3 
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 3'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*

						cap_recovery_factor double precision := capital_recovery_factor;
						capital_cost_factor double precision := 192;
						gas_flow_rate_factor double precision := 0.486;
						retrofit_factor double precision := 1.1;

						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(1028000/ stack_flow_rate) ^ 0.6 * capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
								else
									capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
							end * 0.9383;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- 	Fixed O&M = Gas Flow Rate Factor * Fixed O&M Rate
						-- 		where Gas Flow Rate Factor = 0.486 kW/acfm and Fixed O&M Rate = $6.9/kW-yr.
						fixed_operation_maintenance_cost := 0.486 * 6.9 * stack_flow_rate;

						-- 	Variable O&M = Gas Flow Rate Factor * Variable O&M Rate * Hours per Year * STKFLOW * 60
						-- 		where Gas Flow Rate Factor = 0.486 kW/acf; Variable O&M Rate = $0.0015/kWh; 
						--		Hours per Year = 8,736 hours, STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						variable_operation_maintenance_cost := 0.486 * 0.0015 * 8736 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (3.35 + (0.000729 * 8736)) * stack_flow_rate * 0.9383; (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;

					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(
					case 
						when (' || inv_table_alias || '.stkflow * 60.0) < 1028000 then (1028000/ (' || inv_table_alias || '.stkflow * 60.0)) ^ 0.6
						else 1.0
					end * 192/*capital_cost_factor*/ * 0.486/*gas_flow_rate_factor*/ * 1.1/*retrofit_factor*/ * (' || inv_table_alias || '.stkflow * 60.0) * 0.9383)/*capital_cost*/
					* (case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end)/*cap_recovery_factor*/
					)

				--Equation Type 4
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 4'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (990000 + 9.836 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $75,800
						--		where $75,800 is the fixed O&M cost based on model plant data
						fixed_operation_maintenance_cost := 75800;
						
						--	Variable O&M = $12.82 * STKFLOW * 60
						--		where $12.82 is the variable O&M cost based on model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 12.82 * stack_flow_rate;

						-- calculate operation maintenance cost
						-- operation_maintenance_cost := (75800 + 12.82 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(990000 + 9.836 * (' || inv_table_alias || '.stkflow * 60.0))/*capital_cost*/ 
					* (case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end)/*cap_recovery_factor*/
					)

				--Equation Type 5
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 5'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (2882540 + 244.74 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $749,170
						--		where $749,170 is the fixed O&M cost based on model plant data,
						fixed_operation_maintenance_cost := 749170;

						--	Variable O&M=$148.4 * STKFLOW * 60
						--		where $148.4 is the variable O&M data based on model plant data and credit for recovered product, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 148.40 * stack_flow_rate;
						
						-- 	calculate operation maintenance cost
						--	operation_maintenance_cost := (749170 + 148.40 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(2882540 + 244.74 * (' || inv_table_alias || '.stkflow * 60.0))/*capital_cost*/ 
					* (case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end)/*cap_recovery_factor*/
					)

				--Equation Type 6
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 6'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (3449803 + 135.86 * stack_flow_rate);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						--	Fixed O&M = $797,667
						--		where $797,667 is the fixed O&M cost derived from model plant data
						fixed_operation_maintenance_cost := 797667;

						--	Variable O&M = $58.84 * STKFLOW * 60
						--		where $58.84 is the variable O&M cost derived from model plant data, 
						--		STKFLOW is the stack gas flow rate (ft3/s) from the emissions inventory, 
						--		and 60 is a conversion factor to convert STKFLOW to ft3/min.
						-- 	Darin says that the seconds to minutes conversion is not necessary
						variable_operation_maintenance_cost := 58.84 * stack_flow_rate;

						-- calculate operation maintenance cost
						--	operation_maintenance_cost := (797667 + 58.84 * stack_flow_rate); (previous equation)
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(3449803 + 135.86 * (' || inv_table_alias || '.stkflow * 60.0))/*capital_cost*/ 
					* (case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end)/*cap_recovery_factor*/
					)

				--Equation Type 7
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 7'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when stack_flow_rate < 1028000 then 
									(2882540 + (244.74 * stack_flow_rate) + (((1028000 / stack_flow_rate) ^ 0.6)) * 93.3 * 1.1 * stack_flow_rate * 0.9383)
								else
									(2882540 + (244.74 * stack_flow_rate) + 93.3 * 1.1 * stack_flow_rate * 0.9383)
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate operation maintenance cost
						operation_maintenance_cost := (749170 + (148.40 * stack_flow_rate) + (3.35 + (0.000729 * 8736 ) * (stack_flow_rate) ^ 0.9383));

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					case 
						when (' || inv_table_alias || '.stkflow * 60.0) < 1028000 then 
							(2882540 + (244.74 * (' || inv_table_alias || '.stkflow * 60.0)) + (((1028000 / (' || inv_table_alias || '.stkflow * 60.0)) ^ 0.6)) * 93.3 * 1.1 * (' || inv_table_alias || '.stkflow * 60.0) * 0.9383)
						else
							(2882540 + (244.74 * (' || inv_table_alias || '.stkflow * 60.0)) + 93.3 * 1.1 * (' || inv_table_alias || '.stkflow * 60.0) * 0.9383)
					end/*capital_cost*/ 
					* (case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end)/*cap_recovery_factor*/
					)

				--Equation Type 8
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 8'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then capital_control_cost_factor * stack_flow_rate
								else default_capital_cpt_factor * emis_reduction
							end;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then om_control_cost_factor * stack_flow_rate
								else default_om_cpt_factor * emis_reduction
							end;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  
							case 
								when coalesce(stack_flow_rate, 0) = 0 then null
								when stack_flow_rate >= 5.0 then annualized_capital_cost + 0.04 * capital_cost + operation_maintenance_cost
								else default_annualized_cpt_factor * emis_reduction
							end;

					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					case 
						when (' || inv_table_alias || '.stkflow * 60.0) >= 5.0 then ' || control_measure_equation_table_alias || '.value1/*capital_control_cost_factor*/ * (' || inv_table_alias || '.stkflow * 60.0)
						else ' || control_measure_equation_table_alias || '.value3/*default_capital_cpt_factor*/ * ' || emis_reduction_sql || '
					end 
					* (case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end)/*cap_recovery_factor*/
					)

				-- Equation Type 9
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 9'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := ((total_equipment_cost_factor * stack_flow_rate) + total_equipment_cost_constant) * equipment_to_capital_cost_multiplier;

						-- calculate operation maintenance cost
						operation_maintenance_cost := 
							((electricity_factor * stack_flow_rate) + electricity_constant) + ((dust_disposal_factor * stack_flow_rate) + dust_disposal_constant) + ((bag_replacement_factor * stack_flow_rate) + bag_replacement_constant);

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost :=  annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(
						(
							(
								(' || control_measure_equation_table_alias || '.value1/*total_equipment_cost_factor*/ 
									* (' || inv_table_alias || '.stkflow * 60.0)
								) 
								+ ' || control_measure_equation_table_alias || '.value2/*total_equipment_cost_constant*/
							) * ' || control_measure_equation_table_alias || '.value3/*equipment_to_capital_cost_multiplier*/
						)
					)/*capital_cost*/
					* (case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end)/*cap_recovery_factor*/
					)

				-- Equation Type 10
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 10'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := design_capacity * capital_cost_multiplier * 1000 * (250.0 / design_capacity) ^ capital_cost_exponent;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;


						-- calculate variable_operation_maintenance_cost 
						variable_operation_maintenance_cost := variable_operation_maintenance_cost_multiplier * design_capacity * 0.85 * annual_avg_hours_per_year;
						
						-- calculate fixed_operation_maintenance_cost
						fixed_operation_maintenance_cost := design_capacity * 1000 * fixed_operation_maintenance_cost_multiplier * (250 / design_capacity) ^ fixed_operation_maintenance_cost_exponent;

					--O2 *1000*0.24*(250/O2)^0.3

						-- calculate operation maintenance cost
						operation_maintenance_cost := variable_operation_maintenance_cost + fixed_operation_maintenance_cost;

						-- calculate annual cost
						annual_cost := annualized_capital_cost + operation_maintenance_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, 
							case 
								when length(coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, '''')) = 0 then 
									''MW''::character varying
								else
									' || inv_table_alias || '.design_capacity_unit_numerator
							end
							, coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) * ' || control_measure_equation_table_alias || '.value1/*capital_cost_multiplier*/ * 1000 * (250.0 / public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, 
							case 
								when length(coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, '''')) = 0 then 
									''MW''::character varying
								else
									' || inv_table_alias || '.design_capacity_unit_numerator
							end
							, coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, ''''))) ^ ' || control_measure_equation_table_alias || '.value2/*capital_cost_exponent*/)/*capital_cost*/
					* (case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end)/*cap_recovery_factor*/
					)

				-- Equation Type 11
				when ' || has_design_capacity_columns || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 11'' and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then '
					/*
						-- figure out cost per ton
						computed_cost_per_ton := 
							case 
								when design_capacity <= low_boiler_capacity_range then low_default_cost_per_ton
								when design_capacity > low_boiler_capacity_range and design_capacity < medium_boiler_capacity_range then medium_default_cost_per_ton
								when design_capacity >= medium_boiler_capacity_range then high_default_cost_per_ton
							end;

						-- calculate annual cost
						annual_cost := emis_reduction * computed_cost_per_ton;
						-- calculate capital cost
						capital_cost := annual_cost  * capital_annualized_ratio;
						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;
						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - annualized_capital_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					' || emis_reduction_sql || ' 
					* (case 
						when 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) <= ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value1/*low_default_cost_per_ton*/
						when 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) > ' || control_measure_equation_table_alias || '.value2/*low_boiler_capacity_range*/ and 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) < ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value3/*medium_default_cost_per_ton*/
						when 3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')) >= ' || control_measure_equation_table_alias || '.value4/*medium_boiler_capacity_range*/ then ' || control_measure_equation_table_alias || '.value5/*high_default_cost_per_ton*/
					end)/*computed_cost_per_ton*/ * ' || control_measure_efficiencyrecord_table_alias || '.cap_ann_ratio::double precision
					* (case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end)/*cap_recovery_factor*/
					)

				--Equation Type 12
				when ' || is_point_table || '::boolean and coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 12'' and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 and coalesce(160.0/*stktemp*/, 0) <> 0 then '
					/*
						-- calculate capital cost
						capital_cost := (coalesce(total_capital_investment_fixed_factor, 0.0) + coalesce(total_capital_investment_variable_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000) ^ 0.6;

						-- calculate fixed operation maintenance cost
						fixed_operation_maintenance_cost := (coalesce(annual_operating_cost_fixed_factor, 0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate variable operation maintenance cost
						variable_operation_maintenance_cost := (coalesce(annual_operating_cost_variable_factor,0.0)) * ((stack_flow_rate * 520 / (stack_temperature + 460.0)) / 150000);

						-- calculate operation maintenance cost
						operation_maintenance_cost := fixed_operation_maintenance_cost + variable_operation_maintenance_cost;

						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;

						-- calculate annual cost
						annual_cost := operation_maintenance_cost + annualized_capital_cost;
					*/|| '(' || chained_gdp_adjustment_factor || ' * ' || ref_cost_year_chained_gdp || ' / cast(' || gdplev_table_alias || '.chained_gdp as double precision)) * ' || '
					(
					(
					coalesce(' || control_measure_equation_table_alias || '.value1/*total_capital_investment_fixed_factor*/, 0.0) 
					+ coalesce(' || control_measure_equation_table_alias || '.value2/*total_capital_investment_variable_factor*/, 0.0)
					) 
					* ((' || inv_table_alias || '.stkflow * 60.0/*stack_flow_rate*/ * 520 / (160.0/*stktemp*//*stack_temperature*/ + 460.0)) / 150000) ^ 0.6
					* (case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end)/*cap_recovery_factor*/
					)

				else 
			' else '
		' end || '
					/*
						-- calculate annual cost
						annual_cost := emis_reduction * ref_yr_cost_per_ton;
						-- calculate capital cost
						capital_cost := annual_cost  * capital_annualized_ratio;
						-- calculate annualized capital cost
						annualized_capital_cost := capital_cost * cap_recovery_factor;
						-- calculate operation maintenance cost
						operation_maintenance_cost := annual_cost - coalesce(annualized_capital_cost, 0);
					*/
					(' || emis_reduction_sql || ') * ' || control_measure_efficiencyrecord_table_alias || '.ref_yr_cost_per_ton * ' || control_measure_efficiencyrecord_table_alias || '.cap_ann_ratio::double precision
					* (case 
						when coalesce(' || discount_rate || ', 0) != 0 and coalesce(' || control_measure_table_alias || '.equipment_life, 0) != 0 then public.calculate_capital_recovery_factor(' || discount_rate || ', ' || control_measure_table_alias || '.equipment_life)
						else cap_rec_factor
					end)/*cap_recovery_factor*/
		' || case 
			when use_cost_equations then '
			end 
			' else '
		' end || ')';

	computed_cost_per_ton_expression := 
	'case 
		when coalesce((' || emis_reduction_sql || '), 0) <> 0 then 
			(' || annual_cost_expression || ')
			/*annual_cost*/
			/ (' || emis_reduction_sql || ')
		else null::double precision
	end';


	-- prepare annualized_capital_cost_expression 
	actual_equation_type_expression := 
	case 
		when use_cost_equations then 
			'(case 

				--Equation Type 1 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 1'' then
					case 
						when ' || has_design_capacity_columns || '::boolean and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then 
							''Type 1''
						else
							''-Type 1''
					end

				--Equation Type 2 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 2'' then
					case 
						when ' || has_design_capacity_columns || '::boolean and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 and coalesce(3.412 * public.convert_design_capacity_to_mw(' || inv_table_alias || '.design_capacity, coalesce(' || inv_table_alias || '.design_capacity_unit_numerator, ''''), coalesce(' || inv_table_alias || '.design_capacity_unit_denominator, '''')), 0) <= 2000.0 then 
							''Type 2''
						else
							''-Type 2''
					end

				--Equation Type 3 
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 3'' then
					case 
						when ' || is_point_table || '::boolean and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then 
							''Type 3''
						else
							''-Type 3''
					end

				--Equation Type 4
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 4'' then
					case 
						when ' || is_point_table || '::boolean and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then
							''Type 4''
						else
							''-Type 4''
					end

				--Equation Type 5
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 5'' then
					case 
						when ' || is_point_table || '::boolean and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then
							''Type 5''
						else
							''-Type 5''
					end

				--Equation Type 6
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 6'' then
					case 
						when ' || is_point_table || '::boolean and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then
							''Type 6''
						else
							''-Type 6''
					end

				--Equation Type 7
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 7'' then
					case 
						when ' || is_point_table || '::boolean and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then 
							''Type 7''
						else
							''-Type 7''
					end

				--Equation Type 8
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 8'' then
					case 
						when ' || is_point_table || '::boolean and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then 
							''Type 8''
						else
							''-Type 8''
					end

				-- Equation Type 9
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 9'' then
					case 
						when ' || is_point_table || '::boolean and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 then 
							''Type 9''
						else
							''-Type 9''
					end

				-- Equation Type 10
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 10'' then
					case 
						when ' || has_design_capacity_columns || '::boolean and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then 
							''Type 10''
						else
							''-Type 10''
					end

				-- Equation Type 11
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 11'' then
					case 
						when ' || has_design_capacity_columns || '::boolean and coalesce(' || inv_table_alias || '.design_capacity, 0) <> 0 then 
							''Type 11''
						else
							''-Type 11''
					end

				--Equation Type 12
				when coalesce(' || equation_type_table_alias || '.name,'''') = ''Type 12'' then
					case 
						when ' || is_point_table || '::boolean and coalesce(' || inv_table_alias || '.stkflow, 0) <> 0 and coalesce(160.0/*stktemp*/, 0) <> 0 then 
							''Type 12''
						else
							''-Type 12''
					end

				else 
			' else '
		' end || '
					''''
		' || case 
			when use_cost_equations then '
			end 
			' else '
		' end || ')';

END;
$$ LANGUAGE plpgsql STRICT IMMUTABLE;

/*
select (public.get_cost_expressions(
	141, -- int_control_strategy_id
	7778, -- int_input_dataset_id
	true, --use_cost_equations
	false, --use_override_dataset
	'inv', --inv_table_alias character varying(64), 
	'm', --control_measure_table_alias character varying(64), 
	'et', --equation_type_table_alias character varying(64), 
	'eq', --control_measure_equation_table_alias
	'ef', --control_measure_efficiencyrecord_table_alias
	'csm', --control_strategy_measure_table_alias
	'inv_ovr' --inv_override_table_alias
	)).annual_cost_expression, 
	(public.get_cost_expressions(
	141, -- int_control_strategy_id
	7778, -- int_input_dataset_id
	true, --use_cost_equations
	false, --use_override_dataset
	'inv', --inv_table_alias character varying(64), 
	'm', --control_measure_table_alias character varying(64), 
	'et', --equation_type_table_alias character varying(64), 
	'eq', --control_measure_equation_table_alias
	'ef', --control_measure_efficiencyrecord_table_alias
	'csm', --control_strategy_measure_table_alias
	'inv_ovr' --inv_override_table_alias
	)).capital_cost_expression, 
	(public.get_cost_expressions(
	141, -- int_control_strategy_id
	7778, -- int_input_dataset_id
	true, --use_cost_equations
	false, --use_override_dataset
	'inv', --inv_table_alias character varying(64), 
	'm', --control_measure_table_alias character varying(64), 
	'et', --equation_type_table_alias character varying(64), 
	'eq', --control_measure_equation_table_alias
	'ef', --control_measure_efficiencyrecord_table_alias
	'csm', --control_strategy_measure_table_alias
	'inv_ovr' --inv_override_table_alias
	)).operation_maintenance_cost_expression, 
	(public.get_cost_expressions(
	141, -- int_control_strategy_id
	7778, -- int_input_dataset_id
	true, --use_cost_equations
	false, --use_override_dataset
	'inv', --inv_table_alias character varying(64), 
	'm', --control_measure_table_alias character varying(64), 
	'et', --equation_type_table_alias character varying(64), 
	'eq', --control_measure_equation_table_alias
	'ef', --control_measure_efficiencyrecord_table_alias
	'csm', --control_strategy_measure_table_alias
	'inv_ovr' --inv_override_table_alias
	)).fixed_operation_maintenance_cost_expression, 
	(public.get_cost_expressions(
	141, -- int_control_strategy_id
	7778, -- int_input_dataset_id
	true, --use_cost_equations
	false, --use_override_dataset
	'inv', --inv_table_alias character varying(64), 
	'm', --control_measure_table_alias character varying(64), 
	'et', --equation_type_table_alias character varying(64), 
	'eq', --control_measure_equation_table_alias
	'ef', --control_measure_efficiencyrecord_table_alias
	'csm', --control_strategy_measure_table_alias
	'inv_ovr' --inv_override_table_alias
	)).variable_operation_maintenance_cost_expression, 
	(public.get_cost_expressions(
	141, -- int_control_strategy_id
	7778, -- int_input_dataset_id
	true, --use_cost_equations
	false, --use_override_dataset
	'inv', --inv_table_alias character varying(64), 
	'm', --control_measure_table_alias character varying(64), 
	'et', --equation_type_table_alias character varying(64), 
	'eq', --control_measure_equation_table_alias
	'ef', --control_measure_efficiencyrecord_table_alias
	'csm', --control_strategy_measure_table_alias
	'inv_ovr' --inv_override_table_alias
	)).computed_cost_per_ton_expression;
*/