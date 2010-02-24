-- 12/11/09 - added copied_from column to control_strategies table
ALTER TABLE emf.control_strategies ADD copied_from varchar(255);

-- 12/27/09 - added new equation types for add on control logic.
insert into emf.equation_types (name, description)
select 'Type 9', 'EGU PM Control Equations';
insert into emf.equation_types (name, description)
select 'Type 10', 'ESP Upgrade';
insert into emf.equation_types (name, description)
select 'Type 11', 'SO2 Non EGU Control Equations';


-- 1/8/10 - update Control Strategy Detailed Result Dataset Type QA Step Template Summarize all Control Measures
-- added effrec_count
update emf.dataset_types_qa_step_templates 
set program_arguments = 'select cm.name as CMName, 
	cm.abbreviation as CMabbrev, 
	ct.name as control_technology, 
	s.name as sector, 
	sg.name as source_group, 
	p.name as pollutant, 
	aer.min_efficiency as min_ce, 
	aer.avg_efficiency as mean_ce, 
	aer.max_efficiency as max_ce, 
	aer.min_cost_per_ton as min_cpt, 
	aer.avg_cost_per_ton as mean_cpt, 
	aer.max_cost_per_ton as max_cpt, 
	aer.avg_rule_effectiveness as mean_re, 
	aer.avg_rule_penetration as mean_rp,
	(select count(1) from emf.control_measure_efficiencyrecords where control_measures_id = cm.id and pollutant_id = p.id) as effrec_count
from emf.control_measures cm 
	left outer join emf.control_measure_sectors cms 
	on cms.control_measure_id = cm.id 
	left outer join emf.sectors s 
	on s.id = cms.sector_id 
	left outer join emf.control_technologies ct 
	on ct.id = cm.control_technology 
	left outer join emf.source_groups sg 
	on sg.id = cm.source_group 
	left outer join emf.aggregrated_efficiencyrecords aer 
	on aer.control_measures_id = cm.id 
	left outer join emf.pollutants p 
	on p.id = aer.pollutant_id 
order by cm.name, p.name'
where name = 'Summarize all Control Measures'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result'));

-- 1/9/10 - incorrect unique constraint on eff rec table, existing dev code was missing
ALTER TABLE emf.control_measure_efficiencyrecords
  drop CONSTRAINT control_measure_efficiencyrecords_control_measures_id_key;

ALTER TABLE emf.control_measure_efficiencyrecords
  ADD CONSTRAINT control_measure_efficiencyrecords_control_measures_id_key UNIQUE(control_measures_id, pollutant_id, locale, existing_measure_abbr, existing_dev_code, effective_date, min_emis, max_emis);

-- 1/27/2010, forgot to add variables to new equation types
  
-- Type 10
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'Capital Cost Multiplier', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 10';
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'Capital Cost Exponent', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 10';
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'Variable O&M Cost Multiplier', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 10';
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'Fixed O&M Cost Multiplier', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 10';
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'Fixed O&M Cost Exponent', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 10';

-- 12/29/2009 -- forgot to add equation type variable definition...
-- Type 11
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'Low Default Cost Per Ton', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 11';
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'Low Boiler Capacity Range', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 11';
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'Medium Default Cost Per Ton', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 11';
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'Medium Boiler Capacity Range', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 11';
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'High Default Cost Per Ton', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 11';

-- Type 9
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'Total Equipment Cost Factor', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 9';
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'Total Equipment Cost Constant', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 9';
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'Equipment To Capital Cost Multiplier', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 9';
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'Electricity Factor', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 9';
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'Electricity Constant', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 9';
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'Dust Disposal Factor', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 9';
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'Dust Disposal Constant', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 9';
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'Bag Replacement Factor', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 9';
insert into emf.equation_type_variables (equation_type_id, name, file_col_position, list_index)
select e.id, 'Bag Replacement Constant', (select COALESCE(max(file_col_position) + 1, 1) from emf.equation_type_variables where equation_type_id = e.id) as file_col_position, (select COALESCE(max(list_index) + 1, 0) from emf.equation_type_variables where equation_type_id = e.id) as list_index
from emf.equation_types as e 
where name = 'Type 9';
