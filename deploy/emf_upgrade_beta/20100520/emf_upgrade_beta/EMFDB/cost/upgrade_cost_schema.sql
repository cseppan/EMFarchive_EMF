--Added 02/16/2020
ALTER TABLE emf.equation_types ADD COLUMN inventory_fields varchar(512);
ALTER TABLE emf.equation_types ADD COLUMN equation text;

-- Added 02/24/2010 -- poluates two new equation type table columns -- inventory fields and equation
update emf.equation_types set equation = '', inventory_fields =  'stack_flow_rate' where name = 'Type 9';
update emf.equation_types set equation = '', inventory_fields =  'design_capacity, design_capacity_unit_numerator, design_capacity_unit_denominator' where name = 'Type 11';
update emf.equation_types set equation = '', inventory_fields =  'design_capacity, design_capacity_unit_numerator, design_capacity_unit_denominator, annual_avg_hours_per_year' where name = 'Type 10';
update emf.equation_types set equation = 'Capital Cost= Typical Capital Cost x Min. Stack Flow Rate
O&M Cost= Typical O&M Cost x Min. Stack Flow Rate
Total Cost = Capital Cost x CRF + 0.04 x capital cost + O&M Cost

Notes:
For Min. Stack flow rate less than 5 cfm , default cost per ton cost effectiveness is used.
Min. Stack Flow Rate > 5', inventory_fields =  'stack_flow_rate' where name = 'Type 8';
update emf.equation_types set equation = 'Capital cost = 2882540 + (244.74 x Min. Stack Flow Rate ) + 93.3 x 1.1 x Min. Stack Flow Rate x 0.9383
Capital cost = 2882540 + (244.74 x Min. Stack Flow Rate ) + (((1028000 / Min. Stack Flow Rate) ^ 0.6)) x 93.3 x 1.1 x Min. Stack Flow Rate x 0.9383
O&M Cost = 749170 + (148.40 x Min. Stack Flow Rate) + (3.35 + (0.000729 x 8736 ) x Min. Stack Flow Rate ^ 0.9383)	 
', inventory_fields =  'stack_flow_rate' where name = 'Type 7';
update emf.equation_types set equation = 'Capital cost = 3449803 + (135.86 x Min. Stack Flow rate)
O&M Cost = 797667 + (58.84 x Min. Stack Flow Rate)
Total Cost = Capital Cost x CRF + O&M Cost', inventory_fields =  'stack_flow_rate' where name = 'Type 6';
update emf.equation_types set equation = 'Capital Cost = 2882540 + 244.74 x Min. Stack Flow Rate
O&M Cost = 749170 + 148.40 x Min. Stack Flow Rate
Total Cost = Capital Cost x CRF + O&M Cost', inventory_fields =  'stack_flow_rate' where name = 'Type 5';
update emf.equation_types set equation = 'Capital Cost = 990000 + 9.836 x Min. Stack flow rate O&M Cost = 75800 + 12.82 x Min. Stack Flow Rate
Total Cost = Capital Cost x CRF + O&M Cost

Notes:
Min Stack flow Rate >= 1028000 acfm
Min Stack flow Rate < 1028000 acfm', inventory_fields =  'stack_flow_rate' where name = 'Type 4';
update emf.equation_types set equation = 'Capital Cost  = Capital Cost factor x Gas Flow Rate factor x Retrofit fator x Min. Stack flow rate Capital Cost  = ((1028000/Min. stack flow rate)^0.6)x Capital Cost factor x Gas Flow Rate factor x Retrofit fator x Min. Stack Flow rate
O&M Cost = (3.35 + (0.00729 x 8736)) x Min. stack flow rate x 0.9383
Total Cost = (Capital cost x CRF) + O&M Cost

Notes:
Min Stack Flow Rate >= 1028000 acfm
Min Stack Flow Rate < 1028000 acfm
Capital Cost factor = $192 / kw
Gas flow rate factor = 0.486 KW/acfm', inventory_fields =  'stack_flow_rate' where name = 'Type 3';
update emf.equation_types set equation = 'Annual Cost = Annual Cost Multiplier x (Boiler Capacity) ^ Exponent
Capital Coat = Capital Cost Multiplier x (Boiler Capacity) ^ Exponent', inventory_fields =  'design_capacity, design_capacity_unit_numerator, design_capacity_unit_denominator' where name = 'Type 2';
update emf.equation_types set equation = 'Scaling Factor (SF) = (Model Plant boiler capacity / MW) ^ (Scaling Factor Exponential)
Capital Cost = TCC x NETDC x SF x 1000 Fixed O&M Cost = OMF x NETDC x 1000
Variable O&M Cost = OMV x NETDC x 1000 x CAPFAC x 8760 /1000
CRF = I x (1+ I ) ^ Eq. Life / [(1+ I ) ^ Eq. Life - 1]
Annualized Capital Cost = Capital Cost x CRF
Total Cost = Capital Cost x CRF + O&M Cost

Notes:
Cost equations are based on capacity in the range of > 0 to < 2000 mmBTU/hr. 
If capacity is not within range, a cost per ton value is applied.  Capital cost equations are in the form of $ = capital multiplier (capacity) ^ capital exponent.  Annual costs are in the form of $ = annual multiplier (capacity) ^ annual exponent.  
Multipliers and exponents are available for a no control baseline and a RACT baseline.  Control measure is not applied if boiler capacity is missing.', inventory_fields =  'design_capacity, design_capacity_unit_numerator, design_capacity_unit_denominator' where name = 'Type 1';


-- 02/25/2010 update control measure equipment life to null if its zero
update emf.control_measures
set equipment_life = null
where equipment_life = 0;

-- 04/06/10 Plant Clousre (CSV) dataset type missing keyowrd  
insert into emf.dataset_types_keywords (dataset_type_id, list_index, keyword_id, "value", kwname)
select (select id from emf.dataset_types where "name" = 'Plant Closure (CSV)') as dataset_type_id,
  (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_keywords where dataset_type_id = (select id from emf.dataset_types where "name" = 'Plant Closure (CSV)')) as list_index,
  (select id from emf.keywords where "name" = 'EXPORT_INLINE_COMMENTS') as keyword_id,
  'false' as "value", 'EXPORT_INLINE_COMMENTS' as kwname;

