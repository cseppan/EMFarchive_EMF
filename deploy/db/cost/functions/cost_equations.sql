-- Utility Functions
--select public.convert_design_capacity_to_mw(150, 'MMBtu', 'hr');

CREATE OR REPLACE FUNCTION public.convert_design_capacity_to_mw(design_capacity double precision, design_capacity_unit_numerator character varying, 
	design_capacity_unit_denominator character varying) returns double precision AS $$
DECLARE
	converted_design_capacity double precision;
	unit_numerator character varying;
	unit_denominator character varying;
BEGIN

        --default if not known
        unit_numerator := coalesce(trim(upper(design_capacity_unit_numerator)), '');
        unit_denominator := coalesce(trim(upper(design_capacity_unit_denominator)), '');

        --if you don't know the units then you can't convert the design capacity
        IF length(unit_numerator) = 0 THEN
            return converted_design_capacity;
	END IF;


/* FROM Larry Sorrels at the EPA
        1) E6BTU does mean mmBTU.

        2)  1 MW = 3.412 million BTU/hr (or mmBTU/hr).   And conversely, 1
        mmBTU/hr = 1/3.412 (or 0.2931) MW.

        3)  All of the units listed below are convertible, but some of the
        conversions will be more difficult than others.  The ft3, lb, and ton
        will require some additional conversions to translate mass or volume
        into an energy term such as MW or mmBTU/hr.  Applying some density
        measure (which is mass/volume) will likely be necessary.   Let me know
        if you need help with the conversions. 
*/

        --capacity is already in the right units...
        --no conversion is necessary, these are the expected units.
        IF (unit_numerator = 'MW' and unit_denominator = '') THEN
            return design_capacity;
	END IF;

        IF (unit_numerator = 'MMBTU'
                or unit_numerator = 'E6BTU'
                or unit_numerator = 'BTU'
                or unit_numerator = 'HP'
                or unit_numerator = 'BLRHP') THEN

           --convert numerator unit
		IF (unit_numerator = 'MMBTU'
		    or unit_numerator = 'E6BTU') THEN
			converted_design_capacity := design_capacity / 3.412;
		END IF;
		IF (unit_numerator = 'BTU') THEN
			converted_design_capacity := design_capacity / 3.412 / 1000000.0;
		END IF;
		IF (unit_numerator = 'HP') THEN
                converted_design_capacity := design_capacity * 0.000746;
		END IF;
		IF (unit_numerator = 'BLRHP') THEN
			converted_design_capacity := design_capacity * 0.000981;
		END IF;
--            IF (unit_numerator = 'FT3') THEN
--                converted_design_capacity := design_capacity * 0.000981;

            --convert denominator unit, if missing ASSUME per hr
            IF (unit_denominator = '' or unit_denominator = 'HR'
                    or unit_denominator = 'H') THEN
                return converted_design_capacity;
 		END IF;
           IF (unit_denominator = 'D' or unit_denominator = 'DAY') THEN
                return converted_design_capacity * 24.0;
		END IF;
            IF (unit_denominator = 'M' or unit_denominator = 'MIN') THEN
                return converted_design_capacity / 60.0;
		END IF;
            IF (unit_denominator = 'S' or unit_denominator = 'SEC') THEN
                return converted_design_capacity / 3600.0;
		END IF;
        END IF;
        return null;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION public.calculate_capital_recovery_factor(discount_rate double precision, 
	equipment_life double precision) returns double precision AS $$
DECLARE
BEGIN
	IF coalesce(discount_rate, 0) = 0 or coalesce(equipment_life, 0) = 0 THEN
		return null;
	END IF;

	return (discount_rate * (1 + discount_rate) ^ equipment_life) / ((discount_rate + 1) ^ equipment_life - 1);
END;
$$ LANGUAGE plpgsql IMMUTABLE;



-- Cost Equation Functions

CREATE OR REPLACE FUNCTION public.get_default_costs(
	discount_rate double precision, 
	equipment_life double precision,
	capital_annualized_ratio double precision, 
	capital_recovery_factor double precision, 
	ref_yr_cost_per_ton double precision, 
	emis_reduction double precision, 
	OUT annual_cost double precision, 
	OUT capital_cost double precision,
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision,
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
BEGIN
	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

	-- calculate annual cost
	annual_cost := emis_reduction * ref_yr_cost_per_ton;
	-- calculate capital cost
	capital_cost := annual_cost  * capital_annualized_ratio;
	-- calculate annualized capital cost
	annualized_capital_cost := capital_cost * cap_recovery_factor;
	-- calculate operation maintenance cost
	operation_maintenance_cost := annual_cost - coalesce(annualized_capital_cost, 0);
	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION public.get_type1_equation_costs(
	control_measure_id integer, 
	measure_abbreviation character varying(10),
	discount_rate double precision, 
	equipment_life double precision,
	capital_recovery_factor double precision, 
	emis_reduction double precision, 
	ref_yr_chained_gdp_adjustment_factor double precision, 
	design_capacity double precision,
 	capital_cost_multiplier double precision,
	fixed_om_cost_multiplier double precision,
	variable_om_cost_multiplier double precision,
	scaling_factor_model_size double precision,
	scaling_factor_exponent double precision,
	capacity_factor double precision,
	OUT annual_cost double precision, 
	OUT capital_cost double precision,
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision,
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
	scaling_factor double precision;
	fixed_operation_maintenance_cost double precision;
	variable_operation_maintenance_cost double precision;
BEGIN
	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

	-- calculate scaling factor
	scaling_factor := 
		case 
			when (measure_abbreviation = 'NSCR_UBCW' or measure_abbreviation = 'NSCR_UBCT') and design_capacity >= 600.0 then 1.0
			when design_capacity >= 500.0 then 1.0
			else scaling_factor_model_size ^ scaling_factor_exponent
		end;

	-- calculate capital cost
	capital_cost := ref_yr_chained_gdp_adjustment_factor * capital_cost_multiplier * design_capacity * scaling_factor * 1000;

	-- calculate operation maintenance cost
	-- calculate fixed operation maintenance cost
	fixed_operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * fixed_om_cost_multiplier * design_capacity * 1000;
	-- calculate variable operation maintenance cost
	variable_operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * variable_om_cost_multiplier * design_capacity * capacity_factor * 8760;
	-- calculate total operation maintenance cost
	operation_maintenance_cost := coalesce(fixed_operation_maintenance_cost, 0) + coalesce(variable_operation_maintenance_cost, 0);

	-- calculate annualized capital cost
	annualized_capital_cost := capital_cost * cap_recovery_factor;

	-- calculate annual cost
	annual_cost := annualized_capital_cost + operation_maintenance_cost;

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Type 2
CREATE OR REPLACE FUNCTION public.get_type2_equation_costs(
	control_measure_id integer, 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_recovery_factor double precision, 
	emis_reduction double precision, 
	ref_yr_chained_gdp_adjustment_factor double precision, 
	design_capacity double precision, 
	capital_cost_multiplier double precision,
	capital_cost_exponent double precision, 
	annual_cost_multiplier double precision,
	annual_cost_exponent double precision, 
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
BEGIN
	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

	-- calculate capital cost
	capital_cost := ref_yr_chained_gdp_adjustment_factor * capital_cost_multiplier * design_capacity ^ capital_cost_exponent;

	-- calculate annualized capital cost
	annualized_capital_cost := capital_cost * cap_recovery_factor;

	-- calculate annual cost
	annual_cost := ref_yr_chained_gdp_adjustment_factor * annual_cost_multiplier * design_capacity ^ annual_cost_exponent;

	-- calculate operation maintenance cost
	operation_maintenance_cost := annual_cost - annualized_capital_cost;

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Type 3
CREATE OR REPLACE FUNCTION public.get_type3_equation_costs(
	control_measure_id integer, 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_recovery_factor double precision, 
	emis_reduction double precision, 
	ref_yr_chained_gdp_adjustment_factor double precision, 
	stack_flow_rate double precision, 
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
	capital_cost_factor double precision := 192;
	gas_flow_rate_factor double precision := 0.486;
	retrofit_factor double precision := 1.1;
BEGIN
	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

	-- calculate capital cost
	capital_cost := 
		case 
			when stack_flow_rate < 1028000 then 
				ref_yr_chained_gdp_adjustment_factor * (1028000/ stack_flow_rate) ^ 0.6 * capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
			else
				ref_yr_chained_gdp_adjustment_factor * capital_cost_factor * gas_flow_rate_factor * retrofit_factor * stack_flow_rate
		end;

	-- calculate annualized capital cost
	annualized_capital_cost := capital_cost * cap_recovery_factor;

	-- calculate operation maintenance cost
	operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * (3.35 + (0.00729 * 8736)) * stack_flow_rate * 0.9383;

	-- calculate annual cost
	annual_cost := annualized_capital_cost + operation_maintenance_cost;

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Type 4
CREATE OR REPLACE FUNCTION public.get_type4_equation_costs(
	control_measure_id integer, 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_recovery_factor double precision, 
	emis_reduction double precision, 
	ref_yr_chained_gdp_adjustment_factor double precision, 
	stack_flow_rate double precision, 
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
BEGIN
	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

	-- calculate capital cost
	capital_cost := ref_yr_chained_gdp_adjustment_factor * (990000 + 9.836 * stack_flow_rate);

	-- calculate annualized capital cost
	annualized_capital_cost := capital_cost * cap_recovery_factor;

	-- calculate operation maintenance cost
	operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * (75800 + 12.82 * stack_flow_rate);

	-- calculate annual cost
	annual_cost := annualized_capital_cost + operation_maintenance_cost;

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Type 5
CREATE OR REPLACE FUNCTION public.get_type5_equation_costs(
	control_measure_id integer, 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_recovery_factor double precision, 
	emis_reduction double precision, 
	ref_yr_chained_gdp_adjustment_factor double precision, 
	stack_flow_rate double precision, 
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
BEGIN
	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

	-- calculate capital cost
	capital_cost := ref_yr_chained_gdp_adjustment_factor * (2882540 + 244.74 * stack_flow_rate);

	-- calculate annualized capital cost
	annualized_capital_cost := capital_cost * cap_recovery_factor;

	-- calculate operation maintenance cost
	operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * (749170 + 148.40 * stack_flow_rate);

	-- calculate annual cost
	annual_cost := annualized_capital_cost + operation_maintenance_cost;

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Type 6
CREATE OR REPLACE FUNCTION public.get_type6_equation_costs(
	control_measure_id integer, 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_recovery_factor double precision, 
	emis_reduction double precision, 
	ref_yr_chained_gdp_adjustment_factor double precision, 
	stack_flow_rate double precision, 
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
BEGIN
	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

	-- calculate capital cost
	capital_cost := ref_yr_chained_gdp_adjustment_factor * (3449803 + 135.86 * stack_flow_rate);

	-- calculate annualized capital cost
	annualized_capital_cost := capital_cost * cap_recovery_factor;

	-- calculate operation maintenance cost
	operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * (797667 + 58.84 * stack_flow_rate);

	-- calculate annual cost
	annual_cost := annualized_capital_cost + operation_maintenance_cost;

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Type 7
CREATE OR REPLACE FUNCTION public.get_type7_equation_costs(
	control_measure_id integer, 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_recovery_factor double precision, 
	emis_reduction double precision, 
	ref_yr_chained_gdp_adjustment_factor double precision, 
	stack_flow_rate double precision, 
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
BEGIN
	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

	-- calculate capital cost
	capital_cost := 
		case 
			when stack_flow_rate < 1028000 then 
				ref_yr_chained_gdp_adjustment_factor * (2882540 + (244.74 * stack_flow_rate) + (((1028000 / stack_flow_rate) ^ 0.6)) * 93.3 * 1.1 * stack_flow_rate * 0.9383)
			else
				ref_yr_chained_gdp_adjustment_factor * (2882540 + (244.74 * stack_flow_rate) + 93.3 * 1.1 * stack_flow_rate * 0.9383)
		end;

	-- calculate annualized capital cost
	annualized_capital_cost := capital_cost * cap_recovery_factor;

	-- calculate operation maintenance cost
	operation_maintenance_cost := ref_yr_chained_gdp_adjustment_factor * (749170 + (148.40 * stack_flow_rate) + (3.35 + (0.000729 * 8736 ) * (stack_flow_rate) ^ 0.9383));

	-- calculate annual cost
	annual_cost := annualized_capital_cost + operation_maintenance_cost;

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Type 8
CREATE OR REPLACE FUNCTION public.get_type8_equation_costs(
	control_measure_id integer, 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_recovery_factor double precision, 
	emis_reduction double precision, 
	ref_yr_chained_gdp_adjustment_factor double precision, 
	stack_flow_rate double precision, 
	capital_control_cost_factor double precision,
	om_control_cost_factor double precision,
	default_capital_cpt_factor double precision,
	default_om_cpt_factor double precision,
	default_annualized_cpt_factor double precision,
	OUT annual_cost double precision, 
	OUT capital_cost double precision,
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision,
	OUT computed_cost_per_ton double precision)  AS $$
DECLARE
	cap_recovery_factor double precision := capital_recovery_factor;
BEGIN
	-- * Comments *




	-- * Comments *

	-- get capital recovery factor, caculate if it wasn't passed in...
        IF coalesce(discount_rate, 0) != 0 and coalesce(equipment_life, 0) != 0 THEN 
             cap_recovery_factor := public.calculate_capital_recovery_factor(discount_rate, equipment_life);
        END IF;

	-- calculate capital cost
	capital_cost := 
		case 
			when coalesce(stack_flow_rate, 0) = 0 then null
			when stack_flow_rate >= 5.0 then ref_yr_chained_gdp_adjustment_factor * capital_control_cost_factor * stack_flow_rate
			else ref_yr_chained_gdp_adjustment_factor * default_capital_cpt_factor * emis_reduction
		end;

	-- calculate operation maintenance cost
	operation_maintenance_cost := 
		case 
			when coalesce(stack_flow_rate, 0) = 0 then null
			when stack_flow_rate >= 5.0 then ref_yr_chained_gdp_adjustment_factor * om_control_cost_factor * stack_flow_rate
			else ref_yr_chained_gdp_adjustment_factor * default_om_cpt_factor * emis_reduction
		end;

	-- calculate annualized capital cost
	annualized_capital_cost := capital_cost * cap_recovery_factor;

	-- calculate annual cost
	annual_cost :=  
		case 
			when coalesce(stack_flow_rate, 0) = 0 then null
			when stack_flow_rate >= 5.0 then annualized_capital_cost + 0.04 * capital_cost + operation_maintenance_cost
			else ref_yr_chained_gdp_adjustment_factor * default_annualized_cpt_factor * emis_reduction
		end;

	-- calculate computed cost per ton
	computed_cost_per_ton := 
		case 
			when coalesce(emis_reduction, 0) <> 0 then annual_cost / emis_reduction
			else null
		end;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Cost Equation Factory Method
CREATE OR REPLACE FUNCTION public.get_strategy_costs(
	use_cost_equations boolean, 
	control_measure_id integer, 
	measure_abbreviation character varying(10), 
	discount_rate double precision, 
	equipment_life double precision, 
	capital_annualized_ratio double precision, 
	capital_recovery_factor double precision, 
	ref_yr_cost_per_ton double precision,  
	emis_reduction double precision, 
	ref_yr_chained_gdp_adjustment_factor double precision,
	equation_type character varying(255), 
	variable_coefficient1 double precision, 
	variable_coefficient2 double precision, 
	variable_coefficient3 double precision, 
	variable_coefficient4 double precision, 
	variable_coefficient5 double precision, 
	variable_coefficient6 double precision, 
	variable_coefficient7 double precision, 
	variable_coefficient8 double precision, 
	variable_coefficient9 double precision, 
	variable_coefficient10 double precision, 
	stack_flow_rate double precision, 
	design_capacity double precision, 
	design_capacity_unit_numerator character varying, 
	design_capacity_unit_denominator character varying, 
	OUT annual_cost double precision, 
	OUT capital_cost double precision, 
	OUT operation_maintenance_cost double precision, 
	OUT annualized_capital_cost double precision, 
	OUT computed_cost_per_ton double precision, 
	OUT actual_equation_type character varying(255))  AS $$
DECLARE
	converted_design_capacity double precision;
BEGIN

	-- try cost equations first, then maybe use default approach, if needed
	IF use_cost_equations THEN
		IF equation_type is not null THEN
			-- Type 1
			IF equation_type = 'Type 1' THEN

				converted_design_capacity := public.convert_design_capacity_to_mw(design_capacity, design_capacity_unit_numerator, design_capacity_unit_denominator);

				IF coalesce(design_capacity, 0) <> 0 THEN
					select costs.annual_cost,
						costs.capital_cost,
						costs.operation_maintenance_cost,
						costs.annualized_capital_cost,
						costs.computed_cost_per_ton
					from public.get_type1_equation_costs(control_measure_id, 
						measure_abbreviation,
						discount_rate, 
						equipment_life,
						capital_recovery_factor, 
						emis_reduction, 
						ref_yr_chained_gdp_adjustment_factor, 
						converted_design_capacity,
						variable_coefficient1, 
						variable_coefficient2, 
						variable_coefficient3, 
						variable_coefficient4, 
						variable_coefficient5, 
						variable_coefficient6) as costs
					into annual_cost,
						capital_cost,
						operation_maintenance_cost,
						annualized_capital_cost,
						computed_cost_per_ton;
					IF annual_cost is not null THEN
						actual_equation_type := 'Type 1';
					ELSE
						actual_equation_type := '-Type 1';
					END IF;
					return;
				END IF;
				actual_equation_type := '-Type 1';
			END IF;

			-- Type 2
			IF equation_type = 'Type 2' THEN

				converted_design_capacity := public.convert_design_capacity_to_mw(design_capacity, design_capacity_unit_numerator, design_capacity_unit_denominator);

				IF coalesce(converted_design_capacity, 0) <> 0 THEN
-- design capacity must be less than or equal to 2000 MMBTU/hr (or 586.1665 MW))
					IF (converted_design_capacity <= 586.1665) THEN
						select costs.annual_cost,
							costs.capital_cost,
							costs.operation_maintenance_cost,
							costs.annualized_capital_cost,
							costs.computed_cost_per_ton
						from public.get_type2_equation_costs(control_measure_id, 
							discount_rate, 
							equipment_life, 
							capital_recovery_factor, 
							emis_reduction, 
							ref_yr_chained_gdp_adjustment_factor, 
							converted_design_capacity, 
							variable_coefficient1, 
							variable_coefficient2, 
							variable_coefficient3, 
							variable_coefficient4) as costs
						into annual_cost,
							capital_cost,
							operation_maintenance_cost,
							annualized_capital_cost,
							computed_cost_per_ton;
						IF annual_cost is not null THEN
							actual_equation_type := 'Type 2';
						ELSE
							actual_equation_type := '-Type 2';
						END IF;
						return;
					END IF;
				END IF;
				actual_equation_type := '-Type 2';
			END IF;

			-- Type 3
			IF equation_type = 'Type 3' THEN
				IF coalesce(stack_flow_rate, 0) <> 0 THEN
					select costs.annual_cost,
						costs.capital_cost,
						costs.operation_maintenance_cost,
						costs.annualized_capital_cost,
						costs.computed_cost_per_ton
					from public.get_type3_equation_costs(control_measure_id, 
						discount_rate, 
						equipment_life, 
						capital_recovery_factor, 
						emis_reduction, 
						ref_yr_chained_gdp_adjustment_factor, 
						stack_flow_rate) as costs
					into annual_cost,
						capital_cost,
						operation_maintenance_cost,
						annualized_capital_cost,
						computed_cost_per_ton;
					IF annual_cost is not null THEN
						actual_equation_type := 'Type 3';
					ELSE
						actual_equation_type := '-Type 3';
					END IF;
					return;
				END IF;
				actual_equation_type := '-Type 3';
			END IF;

			-- Type 4
			IF equation_type = 'Type 4' THEN
				IF coalesce(stack_flow_rate, 0) <> 0 THEN
					select costs.annual_cost,
						costs.capital_cost,
						costs.operation_maintenance_cost,
						costs.annualized_capital_cost,
						costs.computed_cost_per_ton
					from public.get_type4_equation_costs(control_measure_id, 
						discount_rate, 
						equipment_life, 
						capital_recovery_factor, 
						emis_reduction, 
						ref_yr_chained_gdp_adjustment_factor, 
						stack_flow_rate) as costs
					into annual_cost,
						capital_cost,
						operation_maintenance_cost,
						annualized_capital_cost,
						computed_cost_per_ton;
					IF annual_cost is not null THEN
						actual_equation_type := 'Type 4';
					ELSE
						actual_equation_type := '-Type 4';
					END IF;
					return;
				END IF;
				actual_equation_type := '-Type 4';
			END IF;

			-- Type 5
			IF equation_type = 'Type 5' THEN
				IF coalesce(stack_flow_rate, 0) <> 0 THEN
					select costs.annual_cost,
						costs.capital_cost,
						costs.operation_maintenance_cost,
						costs.annualized_capital_cost,
						costs.computed_cost_per_ton
					from public.get_type5_equation_costs(control_measure_id, 
						discount_rate, 
						equipment_life, 
						capital_recovery_factor, 
						emis_reduction, 
						ref_yr_chained_gdp_adjustment_factor, 
						stack_flow_rate) as costs
					into annual_cost,
						capital_cost,
						operation_maintenance_cost,
						annualized_capital_cost,
						computed_cost_per_ton;
					IF annual_cost is not null THEN
						actual_equation_type := 'Type 5';
					ELSE
						actual_equation_type := '-Type 5';
					END IF;
					return;
				END IF;
				actual_equation_type := '-Type 5';
			END IF;

			-- Type 6
			IF equation_type = 'Type 6' THEN
				IF coalesce(stack_flow_rate, 0) <> 0 THEN
					select costs.annual_cost,
						costs.capital_cost,
						costs.operation_maintenance_cost,
						costs.annualized_capital_cost,
						costs.computed_cost_per_ton
					from public.get_type6_equation_costs(control_measure_id, 
						discount_rate, 
						equipment_life, 
						capital_recovery_factor, 
						emis_reduction, 
						ref_yr_chained_gdp_adjustment_factor, 
						stack_flow_rate) as costs
					into annual_cost,
						capital_cost,
						operation_maintenance_cost,
						annualized_capital_cost,
						computed_cost_per_ton;
					IF annual_cost is not null THEN
						actual_equation_type := 'Type 6';
					ELSE
						actual_equation_type := '-Type 6';
					END IF;
					return;
				END IF;
				actual_equation_type := '-Type 6';
			END IF;

			-- Type 7
			IF equation_type = 'Type 7' THEN
				IF coalesce(stack_flow_rate, 0) <> 0 THEN
					select costs.annual_cost,
						costs.capital_cost,
						costs.operation_maintenance_cost,
						costs.annualized_capital_cost,
						costs.computed_cost_per_ton
					from public.get_type7_equation_costs(control_measure_id, 
						discount_rate, 
						equipment_life, 
						capital_recovery_factor, 
						emis_reduction, 
						ref_yr_chained_gdp_adjustment_factor, 
						stack_flow_rate) as costs
					into annual_cost,
						capital_cost,
						operation_maintenance_cost,
						annualized_capital_cost,
						computed_cost_per_ton;
					IF annual_cost is not null THEN
						actual_equation_type := 'Type 7';
					ELSE
						actual_equation_type := '-Type 7';
					END IF;
					return;
				END IF;
				actual_equation_type := '-Type 7';
			END IF;

			-- Type 8
			IF equation_type = 'Type 8' THEN
				IF coalesce(stack_flow_rate, 0) <> 0 THEN
					select costs.annual_cost,
						costs.capital_cost,
						costs.operation_maintenance_cost,
						costs.annualized_capital_cost,
						costs.computed_cost_per_ton
					from public.get_type8_equation_costs(control_measure_id, 
						discount_rate, 
						equipment_life, 
						capital_recovery_factor, 
						emis_reduction, 
						ref_yr_chained_gdp_adjustment_factor, 
						stack_flow_rate,
						variable_coefficient1, 
						variable_coefficient2, 
						variable_coefficient3, 
						variable_coefficient4, 
						variable_coefficient5) as costs
					into annual_cost,
						capital_cost,
						operation_maintenance_cost,
						annualized_capital_cost,
						computed_cost_per_ton;
					IF annual_cost is not null THEN
						actual_equation_type := 'Type 8';
					ELSE
						actual_equation_type := '-Type 8';
					END IF;
					return;
				END IF;
				actual_equation_type := '-Type 8';
			END IF;
		END IF;
	END IF;

	select costs.annual_cost,
		costs.capital_cost,
		costs.operation_maintenance_cost,
		costs.annualized_capital_cost,
		costs.computed_cost_per_ton
	from public.get_default_costs(discount_rate, 
		equipment_life, 
		capital_annualized_ratio, 
		capital_recovery_factor, 
		ref_yr_cost_per_ton, 
		emis_reduction) as costs
	into annual_cost,
		capital_cost,
		operation_maintenance_cost,
		annualized_capital_cost,
		computed_cost_per_ton;
--	actual_equation_type := null;
END;
$$ LANGUAGE plpgsql IMMUTABLE;


/*
select public.get_type2_equation_costs(14036, 0.07, 
	20, null, 
	8.76, 1.6, 
	36.635404454865181711606096131301, 33206.3,
	0.65, 2498.1,
	0.73);
select public.convert_design_capacity_to_mw(125, 'E6BTU', 
	null);

select public.get_strategy_costs(true::boolean, 14036, 
	'NSCRIBDO', 0.07, 
	20, 10, 
	null, 2022.25,  
	8.76, 1.6,
	'Type 2', 
	value1, value2, 
	value3, value4, 
	value5, value6, 
	value7, value8, 
	value9, value10, 
	null, 125,'E6BTU', null),value1, value2, 
	value3, value4, 
	value5, value6, 
	value7, value8, 
	value9, value10
from emf.control_measure_equations
where control_measure_id in (
select id
from emf.control_measures
where abbreviation = 'NSCRIBDO'
);	

82400.9;0.65;5555.6;0.79;79002.2;0.65;8701.5;0.65
select *
from emf.control_measure_efficiencyrecords
where control_measures_id in (
select id
from emf.control_measures
where abbreviation = 'NSCRIBCW'
)
select *
from emf.control_measure_equations
where control_measure_id = 13683;

select public.get_equation_type(13683);

select name
from emf.control_measure_sccs
where control_measures_id =13709

select *
from emf.control_measure_efficiencyrecords
where control_measures_id = 13736


*/

/*
select costs.* 
from emf.control_measures
cross join  public.get_strategy_costs(true, 1, 
	0.07, 20.0, 
	1.47, null, 
	2000, 45.5, 
	null, null, 
	null, null) costs) costs3;
*/

