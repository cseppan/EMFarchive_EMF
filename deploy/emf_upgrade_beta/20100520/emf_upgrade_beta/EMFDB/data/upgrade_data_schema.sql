ALTER TABLE emf.users RENAME is_want_emails TO want_emails;

-- added (02/10/2010) to add creator full name to datasets
ALTER TABLE emf.users ADD COLUMN password_reset_date timestamp;

--02/10/2010 add another property of password effective days
INSERT into emf.properties values(DEFAULT, 'PASSWORD_EFFECTIVE_DAYS', '90');

--02/15/2010 add uniqueness constraint on georegions
ALTER TABLE emf.georegions ADD CONSTRAINT georegions_abbr_key UNIQUE (abbr);

--02/25/2010 change the width of phone column in emf.users
ALTER TABLE emf.users ALTER phone TYPE varchar(36);

--03/18/2010 add uniqueness constraint on georegions
ALTER TABLE emf.georegions ALTER COLUMN abbr SET NOT NULL;

--03/19/2010  Create table emf.file_formats
-- emf.file_formats
CREATE TABLE emf.file_formats
(
  id SERIAL PRIMARY KEY,
  name varchar(128) NOT NULL UNIQUE,
  description text,
  delimiter varchar(32),
  fixed_format bool DEFAULT false,
  date_added timestamp without time zone,
  last_modified_date timestamp without time zone,
  creator int4 NOT NULL REFERENCES emf.users (id)
) 
WITHOUT OIDS;
-- emf.file_formats

-- emf.fileformat_columns
CREATE TABLE emf.fileformat_columns
(
  id SERIAL PRIMARY KEY,
  file_format_id int4 NOT NULL REFERENCES emf.file_formats (id),
  list_index int4,
  name varchar(128) NOT NULL,
  type varchar(64) NOT NULL,
  default_value varchar(255),
  description text,
  formatter varchar(64),
  constraints varchar(255),
  mandatory bool DEFAULT false,
  width int DEFAULT 0,
  spaces int DEFAULT 0,
  fix_format_start int DEFAULT 0,
  fix_format_end int DEFAULT 0
) 
WITHOUT OIDS;
-- emf.fileformat_columns

-- create new columns
ALTER TABLE emf.dataset_types ADD COLUMN creation_date timestamp;
ALTER TABLE emf.dataset_types ADD COLUMN last_mod_date timestamp;
ALTER TABLE emf.dataset_types ADD COLUMN creator int4 REFERENCES emf.users (id);
ALTER TABLE emf.dataset_types ADD COLUMN file_format int4 REFERENCES emf.file_formats(id);
insert into emf.keywords ("name") values('REQUIRED_HEADER');

--5/18/10 
insert into emf.keywords ("name") values('INDICES');



-- Summarize by Pollutant - ORL Types
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Pollutant', 1, 'select POLL, sum(ann_emis) as ann_emis from $TABLE[1] e group by POLL order by POLL', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select POLL, sum(ann_emis) as ann_emis from $TABLE[1] e group by POLL order by POLL'
where name = 'Summarize by Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory'));

-- Summarize by Sector and Pollutant - ORL Merged Inventory
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Sector and Pollutant', 1, 'select sector, POLL, sum(ann_emis) as ann_emis from $TABLE[1] e group by sector, POLL order by sector, POLL', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Sector and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select sector, POLL, sum(ann_emis) as ann_emis from $TABLE[1] e group by sector, POLL order by sector, POLL
'
where name = 'Summarize by Sector and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Merged Inventory'));

-- Summarize by Pollutant - Control Strategy Detailed Result
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Pollutant', 1, 
'select POLL, 
	case when coalesce(sum(Emis_Reduction),0) <> 0 then sum(Annual_Cost) / sum(Emis_Reduction) else null end as avg_cost_per_ton, 
	sum(Annual_Cost) as Annual_Cost, 
	sum(Inv_emissions) as Inv_emissions, 
	sum(Final_emissions) as Final_emissions, 
	sum(Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction
from $TABLE[1] e 
group by POLL 
order by POLL', false, 1, ''
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 
'select POLL, 
	case when coalesce(sum(Emis_Reduction),0) <> 0 then sum(Annual_Cost) / sum(Emis_Reduction) else null end as avg_cost_per_ton, 
	sum(Annual_Cost) as Annual_Cost, 
	sum(Inv_emissions) as Inv_emissions, 
	sum(Final_emissions) as Final_emissions, 
	sum(Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction
from $TABLE[1] e 
group by POLL 
order by POLL'
where name = 'Summarize by Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result'));

-- Summarize by Sector and Pollutant - Control Strategy Detailed Result
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Sector and Pollutant', 1, 
'select sector, 
	POLL, 
	case when coalesce(sum(Emis_Reduction),0) <> 0 then sum(Annual_Cost) / sum(Emis_Reduction) else null end as avg_cost_per_ton, 
	sum(Annual_Cost) as Annual_Cost, 
	sum(Inv_emissions) as Inv_emissions, 
	sum(Final_emissions) as Final_emissions, 
	sum(Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction
from $TABLE[1] e 
group by sector, 
	POLL 
order by sector, 
	POLL', false, 1, ''
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Sector and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 
'select sector, 
	POLL, 
	case when coalesce(sum(Emis_Reduction),0) <> 0 then sum(Annual_Cost) / sum(Emis_Reduction) else null end as avg_cost_per_ton, 
	sum(Annual_Cost) as Annual_Cost, 
	sum(Inv_emissions) as Inv_emissions, 
	sum(Final_emissions) as Final_emissions, 
	sum(Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction
from $TABLE[1] e 
group by sector, 
	POLL 
order by sector, 
	POLL'
where name = 'Summarize by Sector and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result'));

-- Summarize by SCC and Pollutant - ORL Types
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by SCC and Pollutant', 1, 'select SCC, POLL, sum(ann_emis) as ann_emis from $TABLE[1] e group by SCC, POLL order by SCC, POLL', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by SCC and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select SCC, POLL, sum(ann_emis) as ann_emis from $TABLE[1] e group by SCC, POLL order by SCC, POLL
'
where name = 'Summarize by SCC and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory'));

-- Summarize by Sector, SCC and Pollutant - ORL Merged Inventory
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Sector, SCC and Pollutant', 1, 'select sector, SCC, POLL, sum(ann_emis) as ann_emis from $TABLE[1] e group by sector, SCC, POLL order by sector, SCC, POLL', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Sector, SCC and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select sector, SCC, POLL, sum(ann_emis) as ann_emis from $TABLE[1] e group by sector, SCC, POLL order by sector, SCC, POLL
'
where name = 'Summarize by Sector, SCC and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Merged Inventory'));

-- Summarize by SCC and Pollutant - Control Strategy Detailed Result
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by SCC and Pollutant', 1, 
'select sector, 
	SCC, 
	POLL, 
	case when coalesce(sum(Emis_Reduction),0) <> 0 then sum(Annual_Cost) / sum(Emis_Reduction) else null end as avg_cost_per_ton, 
	sum(Annual_Cost) as Annual_Cost, 
	sum(Inv_emissions) as Inv_emissions, 
	sum(Final_emissions) as Final_emissions, 
	sum(Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction
from $TABLE[1] e 
group by sector, 
	SCC, 
	POLL 
order by sector, 
	SCC, 
	POLL', false, 1, ''
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by SCC and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 
'select sector, 
	SCC, 
	POLL, 
	case when coalesce(sum(Emis_Reduction),0) <> 0 then sum(Annual_Cost) / sum(Emis_Reduction) else null end as avg_cost_per_ton, 
	sum(Annual_Cost) as Annual_Cost, 
	sum(Inv_emissions) as Inv_emissions, 
	sum(Final_emissions) as Final_emissions, 
	sum(Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction
from $TABLE[1] e 
group by sector, 
	SCC, 
	POLL 
order by sector, 
	SCC, 
	POLL'
where name = 'Summarize by SCC and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result'));

-- Summarize by County and Pollutant - ORL Types
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by County and Pollutant', 1, 'select FIPS, POLL, sum(ann_emis) as ann_emis from $TABLE[1] e group by FIPS, POLL order by FIPS, POLL', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by County and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select FIPS, POLL, sum(ann_emis) as ann_emis from $TABLE[1] e group by FIPS, POLL order by FIPS, POLL
'
where name = 'Summarize by County and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory'));

-- Summarize by Sector, County and Pollutant - ORL Merged Inventory
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Sector, County and Pollutant', 1, 'select sector, FIPS, POLL, sum(ann_emis) as ann_emis from $TABLE[1] e group by sector, FIPS, POLL order by sector, FIPS, POLL', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Sector, County and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select sector, FIPS, POLL, sum(ann_emis) as ann_emis from $TABLE[1] e group by sector, FIPS, POLL order by sector, FIPS, POLL
'
where name = 'Summarize by Sector, County and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Merged Inventory'));

-- Summarize by County and Pollutant - Control Strategy Detailed Result
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by County and Pollutant', 1, 
'select sector, 
	fips.state_county_fips as fips, 
	fips.county, 
	fips.state_name, 
	fips.state_abbr, 
	POLL, 
	case when coalesce(sum(Emis_Reduction),0) <> 0 then sum(Annual_Cost) / sum(Emis_Reduction) else null end as avg_cost_per_ton, 
	sum(e.Annual_Cost) as Annual_Cost, 
	sum(e.Inv_emissions) as Inv_emissions, 
	sum(e.Final_emissions) as Final_emissions, 
	sum(e.Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction,
	fips.centerlon as longitude,
	fips.centerlat as latitude
from $TABLE[1] e 
inner join reference.fips 
	on fips.state_county_fips = e.FIPS 
where fips.country_num = ''0'' 
group by sector, 
	fips.state_county_fips, 
	fips.county, 
	fips.state_name, 
	fips.state_abbr, 
	fips.fipsst, 
	FIPS, 
	POLL,
	fips.centerlon, 
	fips.centerlat
order by sector, 
	fips, 
	POLL', false, 1, ''
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by County and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 
'select sector, 
	fips.state_county_fips as fips, 
	fips.county, 
	fips.state_name, 
	fips.state_abbr, 
	POLL, 
	case when coalesce(sum(Emis_Reduction),0) <> 0 then sum(Annual_Cost) / sum(Emis_Reduction) else null end as avg_cost_per_ton, 
	sum(e.Annual_Cost) as Annual_Cost, 
	sum(e.Inv_emissions) as Inv_emissions, 
	sum(e.Final_emissions) as Final_emissions, 
	sum(e.Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction,
	fips.centerlon as longitude,
	fips.centerlat as latitude
from $TABLE[1] e 
inner join reference.fips 
	on fips.state_county_fips = e.FIPS 
where fips.country_num = ''0'' 
group by sector, 
	fips.state_county_fips, 
	fips.county, 
	fips.state_name, 
	fips.state_abbr, 
	fips.fipsst, 
	FIPS, 
	POLL,
	fips.centerlon, 
	fips.centerlat
order by sector, 
	fips, 
	POLL'
where name = 'Summarize by County and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result'));

-- Summarize by U.S. State and Pollutant - ORL Types
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by U.S. State and Pollutant', 1, 'select fips.state_name, fips.state_abbr, fips.fipsst, e.POLL, sum(ann_emis) as ann_emis from $TABLE[1] e inner join reference.fips on fips.state_county_fips = e.FIPS where fips.country_num = ''0'' group by fips.state_name, fips.state_abbr, fips.fipsst, POLL order by fips.state_name, POLL', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by U.S. State and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select fips.state_name, fips.state_abbr, fips.fipsst, e.POLL, sum(ann_emis) as ann_emis from $TABLE[1] e inner join reference.fips on fips.state_county_fips = e.FIPS where fips.country_num = ''0'' group by fips.state_name, fips.state_abbr, fips.fipsst, POLL order by fips.state_name, POLL
'
where name = 'Summarize by U.S. State and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory'));

-- Summarize by Sector, U.S. State and Pollutant - ORL Merged Inventory
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Sector, U.S. State and Pollutant', 1, 'select e.sector, fips.state_name, fips.state_abbr, fips.fipsst, e.POLL, sum(ann_emis) as ann_emis from $TABLE[1] e inner join reference.fips on fips.state_county_fips = e.FIPS where fips.country_num = ''0'' group by e.sector, fips.state_name, fips.state_abbr, fips.fipsst, POLL order by e.sector, fips.state_name, POLL', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Sector, U.S. State and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select e.sector, fips.state_name, fips.state_abbr, fips.fipsst, e.POLL, sum(ann_emis) as ann_emis from $TABLE[1] e inner join reference.fips on fips.state_county_fips = e.FIPS where fips.country_num = ''0'' group by e.sector, fips.state_name, fips.state_abbr, fips.fipsst, POLL order by e.sector, fips.state_name, POLL
'
where name = 'Summarize by Sector, U.S. State and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Merged Inventory'));

-- Summarize by U.S. State and Pollutant - Control Strategy Detailed Result
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by U.S. State and Pollutant', 1, 
'select e.sector, 
	fips.state_name, 
	fips.state_abbr, 
	fips.fipsst, 
	e.POLL, 
	case when coalesce(sum(Emis_Reduction),0) <> 0 then sum(Annual_Cost) / sum(Emis_Reduction) else null end as avg_cost_per_ton, 
	sum(e.Annual_Cost) as Annual_Cost, 
	sum(e.Inv_emissions) as Inv_emissions, 
	sum(e.Final_emissions) as Final_emissions, 
	sum(e.Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction,
	(fips.state_maxlon + fips.state_minlon) / 2 as longitude, 
	(fips.state_maxlat + fips.state_minlat) / 2 as latitude  
from $TABLE[1] e 
inner join reference.fips 
	on fips.state_county_fips = e.FIPS 
where fips.country_num = ''0''
group by sector, 
	fips.state_name, 
	fips.state_abbr, 
	fips.fipsst, 
	POLL, 
	fips.state_maxlon, 
	fips.state_minlon, 
	fips.state_maxlat, 
	fips.state_minlat 
order by sector, 
	fips.state_name, 
	POLL', false, 1, ''
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by U.S. State and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 
'select e.sector, 
	fips.state_name, 
	fips.state_abbr, 
	fips.fipsst, 
	e.POLL, 
	case when coalesce(sum(Emis_Reduction),0) <> 0 then sum(Annual_Cost) / sum(Emis_Reduction) else null end as avg_cost_per_ton, 
	sum(e.Annual_Cost) as Annual_Cost, 
	sum(e.Inv_emissions) as Inv_emissions, 
	sum(e.Final_emissions) as Final_emissions, 
	sum(e.Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction,
	(fips.state_maxlon + fips.state_minlon) / 2 as longitude, 
	(fips.state_maxlat + fips.state_minlat) / 2 as latitude  
from $TABLE[1] e 
inner join reference.fips 
	on fips.state_county_fips = e.FIPS 
where fips.country_num = ''0''
group by sector, 
	fips.state_name, 
	fips.state_abbr, 
	fips.fipsst, 
	POLL, 
	fips.state_maxlon, 
	fips.state_minlon, 
	fips.state_maxlat, 
	fips.state_minlat 
order by sector, 
	fips.state_name, 
	POLL'
where name = 'Summarize by U.S. State and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result'));

-- Summarize by U.S. County and Pollutant - Control Strategy Detailed Result
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by U.S. County and Pollutant', 1, 
'select e.sector, 
	fips.state_county_fips as fips, 
	fips.county, 
	fips.state_name, 
	fips.state_abbr, 
	e.POLL, 
	case when coalesce(sum(Emis_Reduction),0) <> 0 then sum(Annual_Cost) / sum(Emis_Reduction) else null end as avg_cost_per_ton,
	sum(e.Annual_Cost) as Annual_Cost,
	sum(e.Inv_emissions) as Inv_emissions, 
	sum(e.Final_emissions) as Final_emissions, 
	sum(e.Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction,
	fips.centerlon as longitude, 
	fips.centerlat as latitude 
from $TABLE[1] e 
inner join reference.fips 
	on fips.state_county_fips = e.FIPS 
where fips.country_num = ''0'' 
group by sector, 
	fips.state_county_fips, 
	fips.county, 
	fips.state_name, 
	fips.state_abbr, 
	fips.fipsst, 
	POLL, 
	fips.centerlat, 
	fips.centerlon 
order by sector, 
	fips.state_name, 
	fips.county, 
	POLL', false, 1, ''
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by U.S. County and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 
'select e.sector, 
	fips.state_county_fips as fips, 
	fips.county, 
	fips.state_name, 
	fips.state_abbr, 
	e.POLL, 
	case when coalesce(sum(Emis_Reduction),0) <> 0 then sum(Annual_Cost) / sum(Emis_Reduction) else null end as avg_cost_per_ton,
	sum(e.Annual_Cost) as Annual_Cost,
	sum(e.Inv_emissions) as Inv_emissions, 
	sum(e.Final_emissions) as Final_emissions, 
	sum(e.Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction,
	fips.centerlon as longitude, 
	fips.centerlat as latitude 
from $TABLE[1] e 
inner join reference.fips 
	on fips.state_county_fips = e.FIPS 
where fips.country_num = ''0'' 
group by sector, 
	fips.state_county_fips, 
	fips.county, 
	fips.state_name, 
	fips.state_abbr, 
	fips.fipsst, 
	POLL, 
	fips.centerlat, 
	fips.centerlon 
order by sector, 
	fips.state_name, 
	fips.county, 
	POLL'
where name = 'Summarize by U.S. County and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result'));

-- Summarize by Control Measure and Pollutant - Control Strategy Detailed Result
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Control Measure and Pollutant', 1, 'select e.sector, 
	cm.name as Measure_Name, 
	e.cm_abbrev as Measure_Abbreviation, 
	sg.name as Source_Group, 
	ct.name as Control_Technology, 
	e.POLL, 
	case when coalesce(sum(Emis_Reduction),0) <> 0 then sum(Annual_Cost) / sum(Emis_Reduction) else null end as avg_cost_per_ton, 
	sum(Annual_Cost) as Annual_Cost, 
	sum(Inv_emissions) as Inv_emissions, 
	sum(Final_emissions) as Final_emissions, 
	sum(Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction
from $TABLE[1] e 
left outer join emf.control_measures cm 
	on cm.abbreviation = e.cm_abbrev 
left outer join emf.source_groups sg 
	on sg.id = cm.source_group 
left outer join emf.control_technologies ct 
	on ct.id = cm.control_technology 
group by e.sector, 
	cm.name, 
	e.cm_abbrev, 
	sg.name, 
	ct.name, 
	e.POLL 
order by e.sector, 
	cm.name, 
	POLL', false, 1, ''
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Control Measure and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 
'select e.sector, 
	cm.name as Measure_Name, 
	e.cm_abbrev as Measure_Abbreviation, 
	sg.name as Source_Group, 
	ct.name as Control_Technology, 
	e.POLL, 
	case when coalesce(sum(Emis_Reduction),0) <> 0 then sum(Annual_Cost) / sum(Emis_Reduction) else null end as avg_cost_per_ton, 
	sum(Annual_Cost) as Annual_Cost, 
	sum(Inv_emissions) as Inv_emissions, 
	sum(Final_emissions) as Final_emissions, 
	sum(Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction
from $TABLE[1] e 
left outer join emf.control_measures cm 
	on cm.abbreviation = e.cm_abbrev 
left outer join emf.source_groups sg 
	on sg.id = cm.source_group 
left outer join emf.control_technologies ct 
	on ct.id = cm.control_technology 
group by e.sector, 
	cm.name, 
	e.cm_abbrev, 
	sg.name, 
	ct.name, 
	e.POLL 
order by e.sector, 
	cm.name, 
	POLL'
where name = 'Summarize by Control Measure and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result'));

-- Summarize by Source Group and Pollutant - Control Strategy Detailed Result
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Source Group and Pollutant', 1, 
'select sector, 
	sg.name as Source_Group, 
	e.POLL, 
	case when coalesce(sum(e.Emis_Reduction),0) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null end as avg_cost_per_ton, 
	sum(e.Annual_Cost) as Annual_Cost, 
	sum(e.Inv_emissions) as Inv_emissions, 
	sum(e.Final_emissions) as Final_emissions, 
	sum(e.Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction
from $TABLE[1] e 
left outer join emf.control_measures cm 
	on cm.abbreviation = e.cm_abbrev 
left outer join emf.source_groups sg 
	on sg.id = cm.source_group 
group by sector, 
	sg.name, 
	e.POLL 
order by sector, 
	sg.name, 
	POLL', false, 1, ''
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Source Group and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 
'select sector, 
	sg.name as Source_Group, 
	e.POLL, 
	case when coalesce(sum(e.Emis_Reduction),0) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null end as avg_cost_per_ton, 
	sum(e.Annual_Cost) as Annual_Cost, 
	sum(e.Inv_emissions) as Inv_emissions, 
	sum(e.Final_emissions) as Final_emissions, 
	sum(e.Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction
from $TABLE[1] e 
left outer join emf.control_measures cm 
	on cm.abbreviation = e.cm_abbrev 
left outer join emf.source_groups sg 
	on sg.id = cm.source_group 
group by sector, 
	sg.name, 
	e.POLL 
order by sector, 
	sg.name, 
	POLL'
where name = 'Summarize by Source Group and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result'));

-- Summarize by Control Technology and Pollutant - Control Strategy Detailed Result
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Control Technology and Pollutant', 1, 
'select sector, 
	ct.name as Control_Technology, 
	e.POLL, 
	case when coalesce(sum(Emis_Reduction),0) <> 0 then sum(Annual_Cost) / sum(Emis_Reduction) else null end as avg_cost_per_ton, 
	sum(Annual_Cost) as Annual_Cost, 
	sum(Inv_emissions) as Inv_emissions, 
	sum(Final_emissions) as Final_emissions, 
	sum(Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction
from $TABLE[1] e 
left outer join emf.control_measures cm 
	on cm.abbreviation = e.cm_abbrev 
left outer join emf.control_technologies ct 
	on ct.id = cm.control_technology 
group by sector, 
	ct.name, 
	e.POLL 
order by sector, 
	ct.name, 
	e.POLL', false, 1, ''
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Control Technology and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 
'select sector, 
	ct.name as Control_Technology, 
	e.POLL, 
	case when coalesce(sum(Emis_Reduction),0) <> 0 then sum(Annual_Cost) / sum(Emis_Reduction) else null end as avg_cost_per_ton, 
	sum(Annual_Cost) as Annual_Cost, 
	sum(Inv_emissions) as Inv_emissions, 
	sum(Final_emissions) as Final_emissions, 
	sum(Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction
from $TABLE[1] e 
left outer join emf.control_measures cm 
	on cm.abbreviation = e.cm_abbrev 
left outer join emf.control_technologies ct 
	on ct.id = cm.control_technology 
group by sector, 
	ct.name, 
	e.POLL 
order by sector, 
	ct.name, 
	e.POLL'
where name = 'Summarize by Control Technology and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result'));

-- Summarize by State, SCC, and Control Technology - Control Strategy Detailed Result
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by State, SCC, and Control Technology', 1, 
'select e.sector, 
	ct.name as Control_Technology, 
	e.POLL, 
	e.fipsst, 
	fips.state_abbr, 
	e.scc, 
	case when coalesce(sum(e.Emis_Reduction),0) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null end as avg_cost_per_ton, 
	sum(e.Annual_Cost) as Annual_Cost, 
	sum(e.Inv_emissions) as Inv_emissions,
	sum(e.Final_emissions) as Final_emissions, 
	sum(e.Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions ),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction
from $TABLE[1] e 
left outer join emf.control_measures cm 
	on cm.abbreviation = e.cm_abbrev 
left outer join emf.control_technologies ct 
	on ct.id = cm.control_technology 
inner join reference.fips
	on fips.fipsst = e.fipsst and 
	fips.country_num = ''0''
group by e.sector, 
	ct.name, 
	fips.state_abbr, 
	e.POLL, 
	e.fipsst, 
	e.scc 
order by e.sector, 
	ct.name, 
	e.POLL, 
	e.fipsst, 
	e.scc', false, 1, ''
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by State, SCC, and Control Technology');


update emf.dataset_types_qa_step_templates 
set program_arguments = 
'select e.sector, 
	ct.name as Control_Technology, 
	e.POLL, 
	e.fipsst, 
	fips.state_abbr, 
	e.scc, 
	case when coalesce(sum(e.Emis_Reduction),0) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null end as avg_cost_per_ton, 
	sum(e.Annual_Cost) as Annual_Cost, 
	sum(e.Inv_emissions) as Inv_emissions,
	sum(e.Final_emissions) as Final_emissions, 
	sum(e.Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions ),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction
from $TABLE[1] e 
left outer join emf.control_measures cm 
	on cm.abbreviation = e.cm_abbrev 
left outer join emf.control_technologies ct 
	on ct.id = cm.control_technology 
inner join reference.fips
	on fips.fipsst = e.fipsst and 
	fips.country_num = ''0''
group by e.sector, 
	ct.name, 
	fips.state_abbr, 
	e.POLL, 
	e.fipsst, 
	e.scc 
order by e.sector, 
	ct.name, 
	e.POLL, 
	e.fipsst, 
	e.scc'
where name = 'Summarize by State, SCC, and Control Technology'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result'));

-- Summarize by Control Technology and Pollutant - Strategy Measure Summary
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Control Technology and Pollutant', 1, 'select e.Control_Technology, e.poll, TO_CHAR(case when coalesce(sum(e.Emis_Reduction),0) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null end, ''FM999999999999999990.09'')::double precision as avg_ann_cost_per_ton, TO_CHAR(sum(e.Annual_Cost), ''FM999999999999999990.09'')::double precision as Annual_Cost, TO_CHAR(sum(e.Emis_Reduction), ''FM999999999999999990.09'')::double precision as Emis_Reduction from $TABLE[1] e  group by e.Control_Technology, e.poll order by e.Control_Technology, e.poll', false, 1, ''
from emf.dataset_types dt
where name in ('Strategy Measure Summary')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Control Technology and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select e.Control_Technology, e.poll, TO_CHAR(case when coalesce(sum(e.Emis_Reduction),0) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null end, ''FM999999999999999990.09'')::double precision as avg_ann_cost_per_ton, TO_CHAR(sum(e.Annual_Cost), ''FM999999999999999990.09'')::double precision as Annual_Cost, TO_CHAR(sum(e.Emis_Reduction), ''FM999999999999999990.09'')::double precision as Emis_Reduction from $TABLE[1] e  group by e.Control_Technology, e.poll order by e.Control_Technology, e.poll
'
where name = 'Summarize by Control Technology and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Strategy Measure Summary'));

-- Summarize by Source Group and Pollutant - Strategy Measure Summary
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Source Group and Pollutant', 1, 'select e.source_group, e.poll, TO_CHAR(case when coalesce(sum(e.Emis_Reduction),0) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null end, ''FM999999999999999990.09'')::double precision as avg_ann_cost_per_ton, TO_CHAR(sum(e.Annual_Cost), ''FM999999999999999990.09'')::double precision as Annual_Cost, TO_CHAR(sum(e.Emis_Reduction), ''FM999999999999999990.09'')::double precision as Emis_Reduction from $TABLE[1] e  group by e.source_group, e.poll order by e.source_group, e.poll', false, 1, ''
from emf.dataset_types dt
where name in ('Strategy Measure Summary')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Source Group and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select e.source_group, e.poll, TO_CHAR(case when coalesce(sum(e.Emis_Reduction),0) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null end, ''FM999999999999999990.09'')::double precision as avg_ann_cost_per_ton, TO_CHAR(sum(e.Annual_Cost), ''FM999999999999999990.09'')::double precision as Annual_Cost, TO_CHAR(sum(e.Emis_Reduction), ''FM999999999999999990.09'')::double precision as Emis_Reduction from $TABLE[1] e  group by e.source_group, e.poll order by e.source_group, e.poll
'
where name = 'Summarize by Source Group and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Strategy Measure Summary'));

-- Summarize by Control Measure and Pollutant - Strategy Measure Summary
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Control Measure and Pollutant', 1, 'select e.control_measure_abbreviation, e.control_measure, e.poll, TO_CHAR(case when coalesce(sum(e.Emis_Reduction),0) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null end, ''FM999999999999999990.09'')::double precision as avg_ann_cost_per_ton, TO_CHAR(sum(e.Annual_Cost), ''FM999999999999999990.09'')::double precision as Annual_Cost, TO_CHAR(sum(e.Emis_Reduction), ''FM999999999999999990.09'')::double precision as Emis_Reduction from $TABLE[1] e  group by e.control_measure_abbreviation, e.control_measure, e.poll order by e.control_measure_abbreviation, e.control_measure, e.poll', false, 1, ''
from emf.dataset_types dt
where name in ('Strategy Measure Summary')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Control Measure and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select e.control_measure_abbreviation, e.control_measure, e.poll, TO_CHAR(case when coalesce(sum(e.Emis_Reduction),0) <> 0 then sum(e.Annual_Cost) / sum(e.Emis_Reduction) else null end, ''FM999999999999999990.09'')::double precision as avg_ann_cost_per_ton, TO_CHAR(sum(e.Annual_Cost), ''FM999999999999999990.09'')::double precision as Annual_Cost, TO_CHAR(sum(e.Emis_Reduction), ''FM999999999999999990.09'')::double precision as Emis_Reduction from $TABLE[1] e  group by e.control_measure_abbreviation, e.control_measure, e.poll order by e.control_measure_abbreviation, e.control_measure, e.poll
'
where name = 'Summarize by Control Measure and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Strategy Measure Summary'));

-- Cost Curve - Control Strategy Detailed Result
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Cost Curve', 1, 'select poll, 
	coalesce(ann_cost_per_ton,0) as ann_cost_per_ton, 
	coalesce(incremental_cost_per_ton,0) as inc_cost_per_ton, 
	coalesce(cumulative_emis_reduction,0) as cum_emis_reduction, 
	coalesce(incremental_emis_reduction,0) as inc_emis_reduction, 
	coalesce(cumulative_annual_cost,0) as cum_annual_cost, 
	coalesce(incremental_annual_cost,0) as inc_annual_cost, 
	case when coalesce(incremental_emis_reduction, 0) != 0 then incremental_cost_Per_ton / incremental_emis_reduction else 0.0 end as slope, 
	case when coalesce((select sum(emis_reduction) from $TABLE[1] e where e.poll = tbl.poll), 0) != 0 then cumulative_emis_reduction / (select sum(emis_reduction) from $TABLE[1] e where e.poll = tbl.poll) else null end as pct_emis_red
from (
select poll, 
	d.ann_cost_per_ton, 

	(select sum(e.emis_reduction) 
	from $TABLE[1] e
	where coalesce(e.ann_cost_per_ton,0) <= coalesce(d.ann_cost_per_ton,0)
	) as cumulative_emis_reduction,

	coalesce(ann_cost_per_ton,0) - 
	coalesce((select distinct on (coalesce(f.ann_cost_per_ton,0)) f.ann_cost_per_ton 
		from $TABLE[1] f
		where coalesce(f.ann_cost_per_ton,0) < coalesce(d.ann_cost_per_ton,0)
		order by coalesce(f.ann_cost_per_ton,0) desc
		limit 1),0) as incremental_cost_Per_ton,

	coalesce((select sum(e.emis_reduction) 
	from $TABLE[1] e
	where coalesce(e.ann_cost_per_ton,0) <= coalesce(d.ann_cost_per_ton,0)
	), 0) - 
	coalesce((select sum(e.emis_reduction) 
	from $TABLE[1] e
	where coalesce(e.ann_cost_per_ton,0) <= coalesce((
		select distinct on (coalesce(f.ann_cost_per_ton,0)) f.ann_cost_per_ton 
		from $TABLE[1] f
		where coalesce(f.ann_cost_per_ton,0) < coalesce(d.ann_cost_per_ton,0)
		order by coalesce(f.ann_cost_per_ton,0) desc
		limit 1
		),0)),0) as incremental_emis_reduction,

	(select sum(e.annual_cost) 
	from $TABLE[1] e
	where coalesce(e.ann_cost_per_ton,0) <= coalesce(d.ann_cost_per_ton,0)
	) as cumulative_annual_cost,

	coalesce((select sum(e.annual_cost) 
	from $TABLE[1] e
	where coalesce(e.ann_cost_per_ton,0) <= coalesce(d.ann_cost_per_ton,0)
	), 0) - 
	coalesce((select sum(e.annual_cost) 
	from $TABLE[1] e
	where coalesce(e.ann_cost_per_ton,0) <= coalesce((
		select distinct on (coalesce(f.ann_cost_per_ton,0)) f.ann_cost_per_ton 
		from $TABLE[1] f
		where coalesce(f.ann_cost_per_ton,0) < coalesce(d.ann_cost_per_ton,0)
		order by coalesce(f.ann_cost_per_ton,0) desc
		limit 1
		),0)),0) as incremental_annual_cost

from $TABLE[1] d
WHERE 1 = 1
group by poll, d.ann_cost_per_ton) tbl
order by poll, coalesce(ann_cost_per_ton,0)', false, 1, ''
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Cost Curve');


-- Summarize by SCC and Pollutant with Descriptions - ORL Types
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by SCC and Pollutant with Descriptions', 1, 'select e.SCC, coalesce(s.scc_description,''AN UNSPECIFIED DESCRIPTION'')::character varying(248) as scc_description, e.POLL, coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(11) as pollutant_code_desc, coalesce(p.name,''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name,p.factor, p.voctog, p.species, coalesce(sum(ann_emis),0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis from $TABLE[1] e left outer join reference.invtable p on e.POLL=p.cas left outer join reference.scc s on e.SCC=s.scc group by e.SCC,e.POLL,p.descrptn,s.scc_description, p.name, p.factor,p.voctog, p.species order by e.SCC, p.name', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by SCC and Pollutant with Descriptions');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select e.SCC, coalesce(s.scc_description,''AN UNSPECIFIED DESCRIPTION'')::character varying(248) as scc_description, e.POLL, coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(11) as pollutant_code_desc, coalesce(p.name,''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name,p.factor, p.voctog, p.species, coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis from $TABLE[1] e left outer join reference.invtable p on e.POLL=p.cas left outer join reference.scc s on e.SCC=s.scc group by e.SCC,e.POLL,p.descrptn,s.scc_description, p.name, p.factor,p.voctog, p.species order by e.SCC, p.name
'
where name = 'Summarize by SCC and Pollutant with Descriptions'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory'));

-- Summarize by Sector, SCC and Pollutant with Descriptions - ORL Merged Inventory
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Sector, SCC and Pollutant with Descriptions', 1, 'select e.sector, e.SCC, coalesce(s.scc_description,''AN UNSPECIFIED DESCRIPTION'')::character varying(248) as scc_description, e.POLL, coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(11) as pollutant_code_desc, coalesce(p.name,''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name,p.factor, p.voctog, p.species, coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis from $TABLE[1] e left outer join reference.invtable p on e.POLL=p.cas left outer join reference.scc s on e.SCC=s.scc group by e.sector, e.SCC,e.POLL,p.descrptn,s.scc_description, p.name, p.factor,p.voctog, p.species order by e.sector, e.SCC, p.name', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Sector, SCC and Pollutant with Descriptions');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select e.sector, e.SCC, coalesce(s.scc_description,''AN UNSPECIFIED DESCRIPTION'')::character varying(248) as scc_description, e.POLL, coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(11) as pollutant_code_desc, coalesce(p.name,''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name,p.factor, p.voctog, p.species, coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis from $TABLE[1] e left outer join reference.invtable p on e.POLL=p.cas left outer join reference.scc s on e.SCC=s.scc group by e.sector, e.SCC,e.POLL,p.descrptn,s.scc_description, p.name, p.factor,p.voctog, p.species order by e.sector, e.SCC, p.name
'
where name = 'Summarize by Sector, SCC and Pollutant with Descriptions'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Merged Inventory'));

-- Summarize by U.S. County and Pollutant with Descriptions - ORL Types
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by U.S. County and Pollutant with Descriptions', 1, 'select e.fips, coalesce(fips.county,''AN UNSPECIFIED COUNTY NAME'') as county, 
coalesce(fips.state_abbr,''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, 
e.POLL, coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(11) as pollutant_code_desc, 
coalesce(p.name,''AN UNSPECIFIED DESCRIPTION'') as smoke_name, p.factor, 
p.voctog, p.species, 
TO_CHAR(fips.centerlon, ''FM990.000009'')::double precision as longitude, TO_CHAR(fips.centerlat, ''FM990.000009'')::double precision as latitude,
coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis 
from $TABLE[1] e 
left outer join reference.invtable p 
on e.POLL=p.cas 
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
and fips.country_num = ''0''
group by fips.state_name, fips.state_abbr, substr(e.fips, 1, 2), e.POLL, p.descrptn, p.name, p.factor, p.voctog, p.species, e.fips, fips.county, fips.centerlon, fips.centerlat
order by e.fips, e.POLL', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by U.S. County and Pollutant with Descriptions');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select e.fips, coalesce(fips.county,''AN UNSPECIFIED COUNTY NAME'') as county, 
coalesce(fips.state_abbr,''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, 
e.POLL, coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(11) as pollutant_code_desc, 
coalesce(p.name,''AN UNSPECIFIED DESCRIPTION'') as smoke_name, p.factor, 
p.voctog, p.species, 
TO_CHAR(fips.centerlon, ''FM990.000009'')::double precision as longitude, TO_CHAR(fips.centerlat, ''FM990.000009'')::double precision as latitude,
coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis 
from $TABLE[1] e 
left outer join reference.invtable p 
on e.POLL=p.cas 
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
and fips.country_num = ''0''
group by fips.state_name, fips.state_abbr, substr(e.fips, 1, 2), e.POLL, p.descrptn, p.name, p.factor, p.voctog, p.species, e.fips, fips.county, fips.centerlon, fips.centerlat
order by e.fips, e.POLL
'
where name = 'Summarize by U.S. County and Pollutant with Descriptions'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory'));


-- Summarize by Sector, U.S. County and Pollutant with Descriptions - ORL Merged Inventory
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Sector, U.S. County and Pollutant with Descriptions', 1, 'select e.sector, e.fips, 
coalesce(fips.county,''AN UNSPECIFIED COUNTY NAME'') as county, coalesce(fips.state_abbr,''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, 
coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, e.POLL, 
coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(11) as pollutant_code_desc, coalesce(p.name,''AN UNSPECIFIED DESCRIPTION'') as smoke_name, 
p.factor,p.voctog, 
p.species, 
TO_CHAR(fips.centerlon, ''FM990.000009'')::double precision as longitude, TO_CHAR(fips.centerlat, ''FM990.000009'')::double precision as latitude,
coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis 
from $TABLE[1] e 
left outer join reference.invtable p 
on e.POLL=p.cas 
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
where fips.country_num = ''0'' 
group by e.sector, fips.state_name, fips.state_abbr, substr(e.fips, 1, 2), e.POLL, p.descrptn, p.name, p.factor, p.voctog, p.species, e.fips, fips.county, fips.centerlon, fips.centerlat 
order by e.sector, e.fips, e.POLL', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Sector, U.S. County and Pollutant with Descriptions');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select e.sector, e.fips, 
coalesce(fips.county,''AN UNSPECIFIED COUNTY NAME'') as county, coalesce(fips.state_abbr,''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, 
coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, e.POLL, 
coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(11) as pollutant_code_desc, coalesce(p.name,''AN UNSPECIFIED DESCRIPTION'') as smoke_name, 
p.factor,p.voctog, 
p.species, 
TO_CHAR(fips.centerlon, ''FM990.000009'')::double precision as longitude, TO_CHAR(fips.centerlat, ''FM990.000009'')::double precision as latitude,
coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis 
from $TABLE[1] e 
left outer join reference.invtable p 
on e.POLL=p.cas 
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
where fips.country_num = ''0'' 
group by e.sector, fips.state_name, fips.state_abbr, substr(e.fips, 1, 2), e.POLL, p.descrptn, p.name, p.factor, p.voctog, p.species, e.fips, fips.county, fips.centerlon, fips.centerlat 
order by e.sector, e.fips, e.POLL
'
where name = 'Summarize by Sector, U.S. County and Pollutant with Descriptions'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Merged Inventory'));

-- Summarize by Pollutant with Descriptions - ORL Types
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Pollutant with Descriptions', 1, 'select e.POLL, coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(11) as pollutant_code_desc, coalesce(p.name,''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name, p.factor ,p.voctog, p.species, coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis from $TABLE[1] e left outer join reference.invtable p on e.POLL = p.cas group by e.POLL, p.descrptn, p.name, p.factor, p.voctog, p.species order by p.name', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Pollutant with Descriptions');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select e.POLL, coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(11) as pollutant_code_desc, coalesce(p.name,''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name, p.factor ,p.voctog, p.species, coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis from $TABLE[1] e left outer join reference.invtable p on e.POLL = p.cas group by e.POLL, p.descrptn, p.name, p.factor, p.voctog, p.species order by p.name
'
where name = 'Summarize by Pollutant with Descriptions'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory'));

-- Summarize by Sector, Pollutant with Descriptions - ORL Merged Inventory
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Sector and Pollutant with Descriptions', 1, 'select e.sector, e.POLL, coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(11) as pollutant_code_desc, coalesce(p.name,''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name, p.factor ,p.voctog, p.species, coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis from $TABLE[1] e left outer join reference.invtable p on e.POLL = p.cas group by e.sector, e.POLL, p.descrptn, p.name, p.factor, p.voctog, p.species order by e.sector, p.name', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Sector and Pollutant with Descriptions');



update emf.dataset_types_qa_step_templates 
set program_arguments = 'select e.sector, e.POLL, coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(11) as pollutant_code_desc, coalesce(p.name,''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name, p.factor ,p.voctog, p.species, coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis from $TABLE[1] e left outer join reference.invtable p on e.POLL = p.cas group by e.sector, e.POLL, p.descrptn, p.name, p.factor, p.voctog, p.species order by e.sector, p.name
'
where name = 'Summarize by Sector and Pollutant with Descriptions'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Merged Inventory'));

-- Summarize by Mact Code, U.S. State and Pollutant with Descriptions - ORL Types
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Mact Code, U.S. State and Pollutant with Descriptions', 1, 'select substr(e.fips, 1, 2)::character varying(2) as fipsst, coalesce(fips.state_abbr,''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, 
coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, coalesce(e.mact, ''AN UNSPECIFIED MACT CODE'') as mact_code, 
coalesce(m.mact_source_category, ''AN UNSPECIFIED CATEGORY NAME'') as mact_source_category, e.POLL, 
coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(11) as pollutant_code_desc, coalesce(p.name,''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name, 
p.factor, p.voctog, 
p.species, 
TO_CHAR((fips.state_maxlon + fips.state_minlon) / 2, ''FM990.000009'')::double precision as longitude, 
TO_CHAR((fips.state_maxlat + fips.state_minlat) / 2, ''FM990.000009'')::double precision as latitude, 
coalesce(sum(ann_emis), 0) as ann_emis, 
coalesce(sum(avd_emis), 0) as avd_emis 
from $TABLE[1] e 
left outer join reference.invtable p 
on e.POLL=p.cas 
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
and fips.country_num = ''0'' 
left outer join reference.mact_codes m 
on m.mact_code = e.mact 
group by fips.state_name, fips.state_abbr, substr(e.fips, 1, 2), e.POLL, p.descrptn, p.name, p.factor, p.voctog, p.species, e.mact, m.mact_source_category, fips.state_maxlon, fips.state_minlon, fips.state_maxlat, fips.state_minlat 
order by e.mact, substr(e.fips, 1, 2), e.POLL', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Point Inventory (PTINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Mact Code, U.S. State and Pollutant with Descriptions');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select substr(e.fips, 1, 2)::character varying(2) as fipsst, coalesce(fips.state_abbr,''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, 
coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, coalesce(e.mact, ''AN UNSPECIFIED MACT CODE'') as mact_code, 
coalesce(m.mact_source_category, ''AN UNSPECIFIED CATEGORY NAME'') as mact_source_category, e.POLL, 
coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(11) as pollutant_code_desc, coalesce(p.name,''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name, 
p.factor, p.voctog, 
p.species, 
TO_CHAR((fips.state_maxlon + fips.state_minlon) / 2, ''FM990.000009'')::double precision as longitude, 
TO_CHAR((fips.state_maxlat + fips.state_minlat) / 2, ''FM990.000009'')::double precision as latitude, 
coalesce(sum(ann_emis), 0) as ann_emis, 
coalesce(sum(avd_emis), 0) as avd_emis 
from $TABLE[1] e 
left outer join reference.invtable p 
on e.POLL=p.cas 
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
and fips.country_num = ''0'' 
left outer join reference.mact_codes m 
on m.mact_code = e.mact 
group by fips.state_name, fips.state_abbr, substr(e.fips, 1, 2), e.POLL, p.descrptn, p.name, p.factor, p.voctog, p.species, e.mact, m.mact_source_category, fips.state_maxlon, fips.state_minlon, fips.state_maxlat, fips.state_minlat 
order by e.mact, substr(e.fips, 1, 2), e.POLL
'
where name = 'Summarize by Mact Code, U.S. State and Pollutant with Descriptions'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Point Inventory (PTINV)'));

-- List Data Source Codes and U.S. State with Descriptions - ORL Types
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'List Data Source Codes and U.S. State with Descriptions', 1, 'select substr(e.fips, 1, 2)::character varying(2) as fipsst, coalesce(fips.state_abbr, ''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, 
coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, e.data_source, 
coalesce(d.description, ''AN UNSPECIFIED DESCRIPTION'')::character varying(255) as datasource_desc, 
TO_CHAR((fips.state_maxlon + fips.state_minlon) / 2, ''FM990.000009'')::double precision as longitude, 
TO_CHAR((fips.state_maxlat + fips.state_minlat) / 2, ''FM990.000009'')::double precision as latitude, 
count(1) as count 
from $TABLE[1] e 
	left outer join reference.datasource_codes d 
	on e.data_source=d.code 
	left outer join reference.fips 
	on fips.state_county_fips = e.FIPS 
	and fips.country_num = ''0'' 
group by fips.state_name, fips.state_abbr, substr(e.fips, 1, 2), e.data_source, d.description, fips.state_maxlon, fips.state_minlon, fips.state_maxlat, fips.state_minlat
order by substr(e.fips, 1, 2), e.data_source', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'List Data Source Codes and U.S. State with Descriptions');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select substr(e.fips, 1, 2)::character varying(2) as fipsst, coalesce(fips.state_abbr, ''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, 
coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, e.data_source, 
coalesce(d.description, ''AN UNSPECIFIED DESCRIPTION'')::character varying(255) as datasource_desc, 
TO_CHAR((fips.state_maxlon + fips.state_minlon) / 2, ''FM990.000009'')::double precision as longitude, 
TO_CHAR((fips.state_maxlat + fips.state_minlat) / 2, ''FM990.000009'')::double precision as latitude, 
count(1) as count 
from $TABLE[1] e 
	left outer join reference.datasource_codes d 
	on e.data_source=d.code 
	left outer join reference.fips 
	on fips.state_county_fips = e.FIPS 
	and fips.country_num = ''0'' 
group by fips.state_name, fips.state_abbr, substr(e.fips, 1, 2), e.data_source, d.description, fips.state_maxlon, fips.state_minlon, fips.state_maxlat, fips.state_minlat
order by substr(e.fips, 1, 2), e.data_source
'
where name = 'List Data Source Codes and U.S. State with Descriptions'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)'));

-- Summarize by Data Source Code, U.S. State and Pollutant with Descriptions - ORL Types
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Data Source Code, U.S. State and Pollutant with Descriptions', 1, 'select substr(e.fips, 1, 2)::character varying(2) as fipsst, coalesce(fips.state_abbr, ''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, 
coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, e.data_source, 
coalesce(d.description, ''AN UNSPECIFIED DESCRIPTION'')::character varying(255) as datasource_desc, e.POLL, 
coalesce(p.descrptn, ''AN UNSPECIFIED DESCRIPTION'')::character varying(40) as pollutant_code_desc, coalesce(p.name, ''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name, 
TO_CHAR((fips.state_maxlon + fips.state_minlon) / 2, ''FM990.000009'')::double precision as longitude, 
TO_CHAR((fips.state_maxlat + fips.state_minlat) / 2, ''FM990.000009'')::double precision as latitude, 
coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis 
from $TABLE[1] e 
left outer join reference.datasource_codes d 
on e.data_source=d.code 
left outer join reference.invtable p 
on e.POLL=p.cas 
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
and fips.country_num = ''0''
group by fips.state_name, fips.state_abbr, substr(e.fips, 1, 2), e.poll, e.data_source, d.description, d.description, p.descrptn, p.name, fips.state_maxlon, fips.state_minlon, fips.state_maxlat, fips.state_minlat
order by substr(e.fips, 1, 2), e.data_source', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Data Source Code, U.S. State and Pollutant with Descriptions');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select substr(e.fips, 1, 2)::character varying(2) as fipsst, coalesce(fips.state_abbr, ''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, 
coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, e.data_source, 
coalesce(d.description, ''AN UNSPECIFIED DESCRIPTION'')::character varying(255) as datasource_desc, e.POLL, 
coalesce(p.descrptn, ''AN UNSPECIFIED DESCRIPTION'')::character varying(40) as pollutant_code_desc, coalesce(p.name, ''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name, 
TO_CHAR((fips.state_maxlon + fips.state_minlon) / 2, ''FM990.000009'')::double precision as longitude, 
TO_CHAR((fips.state_maxlat + fips.state_minlat) / 2, ''FM990.000009'')::double precision as latitude, 
coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis 
from $TABLE[1] e 
left outer join reference.datasource_codes d 
on e.data_source=d.code 
left outer join reference.invtable p 
on e.POLL=p.cas 
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
and fips.country_num = ''0''
group by fips.state_name, fips.state_abbr, substr(e.fips, 1, 2), e.poll, e.data_source, d.description, d.description, p.descrptn, p.name, fips.state_maxlon, fips.state_minlon, fips.state_maxlat, fips.state_minlat
order by substr(e.fips, 1, 2), e.data_source
'
where name = 'Summarize by Data Source Code, U.S. State and Pollutant with Descriptions'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)'));

-- Summarize by U.S. State and Pollutant with Descriptions - ORL Types
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by U.S. State and Pollutant with Descriptions', 1, 'select substr(e.fips, 1, 2)::character varying(2) as fipsst, coalesce(fips.state_abbr,''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, 
coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, e.POLL, 
coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(40) as pollutant_code_desc, coalesce(p.name,''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name, 
p.factor, p.voctog, 
p.species, 
TO_CHAR((fips.state_maxlon + fips.state_minlon) / 2, ''FM990.000009'')::double precision as longitude, 
TO_CHAR((fips.state_maxlat + fips.state_minlat) / 2, ''FM990.000009'')::double precision as latitude, 
coalesce(sum(ann_emis), 0) as ann_emis, 
coalesce(sum(avd_emis), 0) as avd_emis 
from $TABLE[1] e 
left outer join reference.invtable p 
on e.POLL=p.cas 
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
and fips.country_num = ''0'' 
group by fips.state_name, fips.state_abbr, substr(e.fips, 1, 2), e.POLL, p.descrptn, p.name, p.factor, p.voctog, p.species, fips.state_maxlon, fips.state_minlon, fips.state_maxlat, fips.state_minlat
order by substr(e.fips, 1, 2)', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by U.S. State and Pollutant with Descriptions');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select substr(e.fips, 1, 2)::character varying(2) as fipsst, coalesce(fips.state_abbr,''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, 
coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, e.POLL, 
coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(40) as pollutant_code_desc, coalesce(p.name,''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name, 
p.factor, p.voctog, 
p.species, 
TO_CHAR((fips.state_maxlon + fips.state_minlon) / 2, ''FM990.000009'')::double precision as longitude, 
TO_CHAR((fips.state_maxlat + fips.state_minlat) / 2, ''FM990.000009'')::double precision as latitude, 
coalesce(sum(ann_emis), 0) as ann_emis, 
coalesce(sum(avd_emis), 0) as avd_emis 
from $TABLE[1] e 
left outer join reference.invtable p 
on e.POLL=p.cas 
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
and fips.country_num = ''0'' 
group by fips.state_name, fips.state_abbr, substr(e.fips, 1, 2), e.POLL, p.descrptn, p.name, p.factor, p.voctog, p.species, fips.state_maxlon, fips.state_minlon, fips.state_maxlat, fips.state_minlat
order by substr(e.fips, 1, 2)
'
where name = 'Summarize by U.S. State and Pollutant with Descriptions'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory'));

-- Summarize by Sector, U.S. State and Pollutant with Descriptions - ORL Merged Inventory
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Sector, U.S. State and Pollutant with Descriptions', 1, 'select e.sector, substr(e.fips, 1, 2)::character varying(2) as fipsst, 
coalesce(fips.state_abbr,''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, 
e.POLL, coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(11) as pollutant_code_desc, 
coalesce(p.name,''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name, p.factor, 
p.voctog, p.species, 
TO_CHAR((fips.state_maxlon + fips.state_minlon) / 2, ''FM990.000009'')::double precision as longitude, 
TO_CHAR((fips.state_maxlat + fips.state_minlat) / 2, ''FM990.000009'')::double precision as latitude, 
coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis 
from $TABLE[1] e 
left outer join reference.invtable p 
on e.POLL=p.cas 
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
and fips.country_num = ''0'' 
group by e.sector, fips.state_name, fips.state_abbr, substr(e.fips, 1, 2), e.POLL, p.descrptn, p.name, p.factor, p.voctog, p.species, fips.state_maxlon, fips.state_minlon, fips.state_maxlat, fips.state_minlat
order by e.sector, substr(e.fips, 1, 2)', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Sector, U.S. State and Pollutant with Descriptions');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select e.sector, substr(e.fips, 1, 2)::character varying(2) as fipsst, 
coalesce(fips.state_abbr,''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, 
e.POLL, coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(11) as pollutant_code_desc, 
coalesce(p.name,''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name, p.factor, 
p.voctog, p.species, 
TO_CHAR((fips.state_maxlon + fips.state_minlon) / 2, ''FM990.000009'')::double precision as longitude, 
TO_CHAR((fips.state_maxlat + fips.state_minlat) / 2, ''FM990.000009'')::double precision as latitude, 
coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis 
from $TABLE[1] e 
left outer join reference.invtable p 
on e.POLL=p.cas 
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
and fips.country_num = ''0'' 
group by e.sector, fips.state_name, fips.state_abbr, substr(e.fips, 1, 2), e.POLL, p.descrptn, p.name, p.factor, p.voctog, p.species, fips.state_maxlon, fips.state_minlon, fips.state_maxlat, fips.state_minlat
order by e.sector, substr(e.fips, 1, 2)
'
where name = 'Summarize by Sector, U.S. State and Pollutant with Descriptions'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Merged Inventory'));

-- Summarize by U.S. State, SCC and Pollutant with Descriptions - ORL Types
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by U.S. State, SCC and Pollutant with Descriptions', 1, 'select substr(e.fips, 1, 2)::character varying(2) as fipsst, coalesce(fips.state_abbr,''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, 
coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, e.SCC, 
coalesce(s.scc_description,''AN UNSPECIFIED DESCRIPTION'')::character varying(248) as scc_description, e.POLL, 
coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(11) as pollutant_code_desc, coalesce(p.name,''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name, 
p.factor, p.voctog, 
p.species, 
TO_CHAR((fips.state_maxlon + fips.state_minlon) / 2, ''FM990.000009'')::double precision as longitude, 
TO_CHAR((fips.state_maxlat + fips.state_minlat) / 2, ''FM990.000009'')::double precision as latitude, 
coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis 
from $TABLE[1] e
left outer join reference.invtable p 
on e.POLL=p.cas 
left outer join reference.scc s 
on e.SCC=s.scc 
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
and fips.country_num = ''0'' 
group by substr(e.fips, 1, 2), fips.state_abbr, fips.state_name, e.SCC,e.POLL,p.descrptn,s.scc_description, p.name, p.factor,p.voctog, p.species, fips.state_maxlon, fips.state_minlon, fips.state_maxlat, fips.state_minlat
order by substr(e.fips, 1, 2), e.SCC, e.POLL', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by U.S. State, SCC and Pollutant with Descriptions');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select substr(e.fips, 1, 2)::character varying(2) as fipsst, coalesce(fips.state_abbr,''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, 
coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, e.SCC, 
coalesce(s.scc_description,''AN UNSPECIFIED DESCRIPTION'')::character varying(248) as scc_description, e.POLL, 
coalesce(p.descrptn,''AN UNSPECIFIED DESCRIPTION'')::character varying(11) as pollutant_code_desc, coalesce(p.name,''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name, 
p.factor, p.voctog, 
p.species, 
TO_CHAR((fips.state_maxlon + fips.state_minlon) / 2, ''FM990.000009'')::double precision as longitude, 
TO_CHAR((fips.state_maxlat + fips.state_minlat) / 2, ''FM990.000009'')::double precision as latitude, 
coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis 
from $TABLE[1] e
left outer join reference.invtable p 
on e.POLL=p.cas 
left outer join reference.scc s 
on e.SCC=s.scc 
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
and fips.country_num = ''0'' 
group by substr(e.fips, 1, 2), fips.state_abbr, fips.state_name, e.SCC,e.POLL,p.descrptn,s.scc_description, p.name, p.factor,p.voctog, p.species, fips.state_maxlon, fips.state_minlon, fips.state_maxlat, fips.state_minlat
order by substr(e.fips, 1, 2), e.SCC, e.POLL
'
where name = 'Summarize by U.S. State, SCC and Pollutant with Descriptions'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory'));

-- Summarize by Sector, U.S. State, SCC and Pollutant with Descriptions - ORL Merged Inventory
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Sector, U.S. State, SCC and Pollutant with Descriptions', 1, 'select e.sector, substr(e.fips, 1, 2)::character varying(2) as fipsst, 
coalesce(fips.state_abbr, ''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, 
e.SCC, coalesce(s.scc_description, ''AN UNSPECIFIED DESCRIPTION'')::character varying(248) as scc_description, 
e.POLL, coalesce(p.descrptn, ''AN UNSPECIFIED DESCRIPTION'')::character varying(40) as pollutant_code_desc, 
coalesce(p.name, ''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name, p.factor,
p.voctog, p.species, 
TO_CHAR((fips.state_maxlon + fips.state_minlon) / 2, ''FM990.000009'')::double precision as longitude, 
TO_CHAR((fips.state_maxlat + fips.state_minlat) / 2, ''FM990.000009'')::double precision as latitude, 
coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis 
from $TABLE[1] e
left outer join reference.invtable p 
on e.POLL=p.cas 
left outer join reference.scc s 
on e.SCC=s.scc 
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
where fips.country_num = ''0''
group by e.sector, substr(e.fips, 1, 2), fips.state_abbr, fips.state_name, e.SCC,e.POLL,p.descrptn,s.scc_description, p.name, p.factor,p.voctog, p.species, fips.state_maxlon, fips.state_minlon, fips.state_maxlat, fips.state_minlat
order by e.sector, substr(e.fips, 1, 2), e.SCC, e.POLL', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Sector, U.S. State, SCC and Pollutant with Descriptions');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select e.sector, substr(e.fips, 1, 2)::character varying(2) as fipsst, 
coalesce(fips.state_abbr, ''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, 
e.SCC, coalesce(s.scc_description, ''AN UNSPECIFIED DESCRIPTION'')::character varying(248) as scc_description, 
e.POLL, coalesce(p.descrptn, ''AN UNSPECIFIED DESCRIPTION'')::character varying(40) as pollutant_code_desc, 
coalesce(p.name, ''AN UNSPECIFIED SMOKE NAME'')::character varying(11) as smoke_name, p.factor,
p.voctog, p.species, 
TO_CHAR((fips.state_maxlon + fips.state_minlon) / 2, ''FM990.000009'')::double precision as longitude, 
TO_CHAR((fips.state_maxlat + fips.state_minlat) / 2, ''FM990.000009'')::double precision as latitude, 
coalesce(sum(ann_emis), 0) as ann_emis, coalesce(sum(avd_emis), 0) as avd_emis 
from $TABLE[1] e
left outer join reference.invtable p 
on e.POLL=p.cas 
left outer join reference.scc s 
on e.SCC=s.scc 
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
where fips.country_num = ''0''
group by e.sector, substr(e.fips, 1, 2), fips.state_abbr, fips.state_name, e.SCC,e.POLL,p.descrptn,s.scc_description, p.name, p.factor,p.voctog, p.species, fips.state_maxlon, fips.state_minlon, fips.state_maxlat, fips.state_minlat
order by e.sector, substr(e.fips, 1, 2), e.SCC, e.POLL
'
where name = 'Summarize by Sector, U.S. State, SCC and Pollutant with Descriptions'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Merged Inventory'));

-- Summarize by U.S. State and SMOKE Pollutant Name - ORL Types
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by U.S. State and SMOKE Pollutant Name', 1, 'select substr(e.fips, 1, 2)::character varying(2) as fipsst, coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, 
coalesce(fips.state_abbr,''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, r.name, 
r.species, r.voctog, 
TO_CHAR((fips.state_maxlon + fips.state_minlon) / 2, ''FM990.000009'')::double precision as longitude, 
TO_CHAR((fips.state_maxlat + fips.state_minlat) / 2, ''FM990.000009'')::double precision as latitude, 
coalesce(sum(cast(r.factor as double precision) * ann_emis), 0) as ann_emis, coalesce(sum(cast(r.factor as double precision) * avd_emis), 0) as avd_emis 
from $TABLE[1] e
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
inner join reference.invtable r on e.poll = r.cas 
and fips.country_num = ''0'' 
group by fips.state_name, fips.state_abbr, substr(e.fips, 1, 2), r.name, r.voctog, r.species, fips.state_maxlon, fips.state_minlon, fips.state_maxlat, fips.state_minlat
order by substr(e.fips, 1, 2),  r.species, r.voctog, r.name', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by U.S. State and SMOKE Pollutant Name');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select substr(e.fips, 1, 2)::character varying(2) as fipsst, coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, 
coalesce(fips.state_abbr,''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, r.name, 
r.species, r.voctog, 
TO_CHAR((fips.state_maxlon + fips.state_minlon) / 2, ''FM990.000009'')::double precision as longitude, 
TO_CHAR((fips.state_maxlat + fips.state_minlat) / 2, ''FM990.000009'')::double precision as latitude, 
coalesce(sum(cast(r.factor as double precision) * ann_emis), 0) as ann_emis, coalesce(sum(cast(r.factor as double precision) * avd_emis), 0) as avd_emis 
from $TABLE[1] e
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
inner join reference.invtable r on e.poll = r.cas 
and fips.country_num = ''0'' 
group by fips.state_name, fips.state_abbr, substr(e.fips, 1, 2), r.name, r.voctog, r.species, fips.state_maxlon, fips.state_minlon, fips.state_maxlat, fips.state_minlat
order by substr(e.fips, 1, 2),  r.species, r.voctog, r.name
'
where name = 'Summarize by U.S. State and SMOKE Pollutant Name'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)','ORL Point Inventory (PTINV)','ORL Merged Inventory'));

-- Summarize by Sector, U.S. State and SMOKE Pollutant Name - ORL Merged Inventory
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Sector, U.S. State and SMOKE Pollutant Name', 1, 'select e.sector, substr(e.fips, 1, 2)::character varying(2) as fipsst, 
coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, coalesce(fips.state_abbr,''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, 
r.name, r.species, 
r.voctog, 
TO_CHAR((fips.state_maxlon + fips.state_minlon) / 2, ''FM990.000009'')::double precision as longitude, 
TO_CHAR((fips.state_maxlat + fips.state_minlat) / 2, ''FM990.000009'')::double precision as latitude, 
coalesce(sum(cast(r.factor as double precision) * ann_emis), 0) as ann_emis, 
coalesce(sum(cast(r.factor as double precision) * avd_emis), 0) as avd_emis 
from $TABLE[1] e
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
and fips.country_num = ''0'' 
inner join reference.invtable r 
on e.poll = r.cas 
group by e.sector, fips.state_name, fips.state_abbr, substr(e.fips, 1, 2), r.name, r.voctog, r.species, fips.state_maxlon, fips.state_minlon, fips.state_maxlat, fips.state_minlat
order by e.sector, substr(e.fips, 1, 2),  r.species, r.voctog, r.name', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Sector, U.S. State and SMOKE Pollutant Name');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select e.sector, substr(e.fips, 1, 2)::character varying(2) as fipsst, 
coalesce(fips.state_name, ''AN UNSPECIFIED STATE NAME'')::character varying(100) as state_name, coalesce(fips.state_abbr,''AN UNSPECIFIED STATE ABBREVIATION'')::character varying(2) as state_abbr, 
r.name, r.species, 
r.voctog, 
TO_CHAR((fips.state_maxlon + fips.state_minlon) / 2, ''FM990.000009'')::double precision as longitude, 
TO_CHAR((fips.state_maxlat + fips.state_minlat) / 2, ''FM990.000009'')::double precision as latitude, 
coalesce(sum(cast(r.factor as double precision) * ann_emis), 0) as ann_emis, 
coalesce(sum(cast(r.factor as double precision) * avd_emis), 0) as avd_emis 
from $TABLE[1] e
left outer join reference.fips 
on fips.state_county_fips = e.FIPS 
and fips.country_num = ''0'' 
inner join reference.invtable r 
on e.poll = r.cas 
group by e.sector, fips.state_name, fips.state_abbr, substr(e.fips, 1, 2), r.name, r.voctog, r.species, fips.state_maxlon, fips.state_minlon, fips.state_maxlat, fips.state_minlat
order by e.sector, substr(e.fips, 1, 2),  r.species, r.voctog, r.name
'
where name = 'Summarize by Sector, U.S. State and SMOKE Pollutant Name'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Merged Inventory'));

-- Compare CoST to NEI measures - 'ORL Point Inventory (PTINV)','ORL Merged Inventory'
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Compare CoST to NEI measures', 1, 'select e.scc, fips, plantid, pointid, stackid, segment, poll, ann_emis, avd_emis,
	ceff as inv_ceff,
	(string_to_array(e.control_measures::text, ''&''))[array_upper(string_to_array(e.control_measures::text, ''&''), 1)] as measure_abbreviation,
	cm.name as measure_name,
	(string_to_array(pct_reduction::text, ''&''))[array_upper(string_to_array(pct_reduction::text, ''&''), 1)] as measure_ceff,
	abs(ceff - (string_to_array(pct_reduction::text, ''&''))[array_upper(string_to_array(pct_reduction::text, ''&''), 1)]::double precision) as ceff_abs_diff,
	e.cpri,
	cd.control_device_desc as pri_control_device,
	e.csec,
	cd2.control_device_desc as sec_control_device, 
	scc_description

from $TABLE[1] e

	left outer join emf.control_measures cm
	on cm.abbreviation = (string_to_array(e.control_measures::text, ''&''))[array_upper(string_to_array(e.control_measures::text, ''&''), 1)]

	left outer join reference.control_device cd2
	on cd2.control_device_code::integer = e.csec

	left outer join reference.control_device cd
	on cd.control_device_code::integer = e.cpri

	left outer join reference.scc sccs
	on sccs.scc = e.scc
where coalesce(ceff, 0.0) > 0 
	or length(control_measures) > 0
order by abs(ceff - (string_to_array(pct_reduction::text, ''&''))[array_upper(string_to_array(pct_reduction::text, ''&''), 1)]::double precision)', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Point Inventory (PTINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Compare CoST to NEI measures');

-- Compare CoST to NEI measures - 'ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)'
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Compare CoST to NEI measures', 1, 'select e.scc, fips, poll, ann_emis, avd_emis,
	ceff as inv_ceff,
	(string_to_array(e.control_measures::text, ''&''))[array_upper(string_to_array(e.control_measures::text, ''&''), 1)] as measure_abbreviation,
	cm.name as measure_name,
	(string_to_array(pct_reduction::text, ''&''))[array_upper(string_to_array(pct_reduction::text, ''&''), 1)] as measure_ceff,
	abs(ceff - (string_to_array(pct_reduction::text, ''&''))[array_upper(string_to_array(pct_reduction::text, ''&''), 1)]::double precision) as ceff_abs_diff,
	e.PRIMARY_DEVICE_TYPE_CODE as cpri,
	cd.control_device_desc as pri_control_device,
	e.SECONDARY_DEVICE_TYPE_CODE as csec,
	cd2.control_device_desc as sec_control_device, 
	scc_description

from $TABLE[1] e

	left outer join emf.control_measures cm
	on cm.abbreviation = (string_to_array(e.control_measures::text, ''&''))[array_upper(string_to_array(e.control_measures::text, ''&''), 1)]

	left outer join reference.control_device cd2
	on cd2.control_device_code::integer = e.SECONDARY_DEVICE_TYPE_CODE

	left outer join reference.control_device cd
	on cd.control_device_code::integer = e.PRIMARY_DEVICE_TYPE_CODE

	left outer join reference.scc sccs
	on sccs.scc = e.scc
where coalesce(ceff, 0.0) > 0 
	or length(control_measures) > 0
order by abs(ceff - (string_to_array(pct_reduction::text, ''&''))[array_upper(string_to_array(pct_reduction::text, ''&''), 1)]::double precision)', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Compare CoST to NEI measures');


-- Compare CoST to NEI measures - 'ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)'
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Compare CoST to NEI measures', 1, 'select e.scc, fips, poll, ann_emis, avd_emis,
	ceff as inv_ceff,
	(string_to_array(e.control_measures::text, ''&''))[array_upper(string_to_array(e.control_measures::text, ''&''), 1)] as measure_abbreviation,
	cm.name as measure_name,
	(string_to_array(pct_reduction::text, ''&''))[array_upper(string_to_array(pct_reduction::text, ''&''), 1)] as measure_ceff,
	abs(ceff - (string_to_array(pct_reduction::text, ''&''))[array_upper(string_to_array(pct_reduction::text, ''&''), 1)]::double precision) as ceff_abs_diff, 
	scc_description

from $TABLE[1] e

	left outer join emf.control_measures cm
	on cm.abbreviation = (string_to_array(e.control_measures::text, ''&''))[array_upper(string_to_array(e.control_measures::text, ''&''), 1)]

	left outer join reference.scc sccs
	on sccs.scc = e.scc
where coalesce(ceff, 0.0) > 0 
	or length(control_measures) > 0
order by abs(ceff - (string_to_array(pct_reduction::text, ''&''))[array_upper(string_to_array(pct_reduction::text, ''&''), 1)]::double precision)', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Compare CoST to NEI measures');

-- Compare CoST to NEI measures - 'ORL Point Inventory (PTINV)','ORL Merged Inventory'
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Roll Up CoST and NEI measures', 1, 'select distinct e.cpri as inv_pri_control_device_code,
	cd.control_device_desc as inv_pri_control_device,
	(string_to_array(e.control_measures::text, ''&''))[array_upper(string_to_array(e.control_measures::text, ''&''), 1)] as cost_measure_abbreviation,
	cm.name as cost_measure_name,
	ct."name" as cost_control_technology
	
from $TABLE[1] e

	left outer join emf.control_measures cm
	on cm.abbreviation = (string_to_array(e.control_measures::text, ''&''))[array_upper(string_to_array(e.control_measures::text, ''&''), 1)]

	left outer join emf.control_technologies ct
	on ct.id = cm.control_technology

	left outer join reference.control_device cd
	on cd.control_device_code::integer = e.cpri

	left outer join reference.scc sccs
	on sccs.scc = e.scc
where coalesce(ceff, 0.0) > 0 
	and length(control_measures) > 0
order by e.cpri,
	cd.control_device_desc,
	cm.name,
	ct."name"', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Point Inventory (PTINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Roll Up CoST and NEI measures');

-- Compare CoST to NEI measures - 'ORL Nonpoint Inventory (ARINV)'
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Roll Up CoST and NEI measures', 1, 'select distinct e.PRIMARY_DEVICE_TYPE_CODE as inv_pri_control_device_code,
	cd.control_device_desc as inv_pri_control_device,
	(string_to_array(e.control_measures::text, ''&''))[array_upper(string_to_array(e.control_measures::text, ''&''), 1)] as cost_measure_abbreviation,
	cm.name as cost_measure_name,
	ct."name" as cost_control_technology
	
from $TABLE[1] e

	left outer join emf.control_measures cm
	on cm.abbreviation = (string_to_array(e.control_measures::text, ''&''))[array_upper(string_to_array(e.control_measures::text, ''&''), 1)]

	left outer join emf.control_technologies ct
	on ct.id = cm.control_technology

	left outer join reference.control_device cd
	on cd.control_device_code::integer = e.PRIMARY_DEVICE_TYPE_CODE

	left outer join reference.scc sccs
	on sccs.scc = e.scc
where coalesce(ceff, 0.0) > 0 
	and length(control_measures) > 0
order by e.cpri,
	cd.control_device_desc,
	cm.name,
	ct."name"', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Roll Up CoST and NEI measures');


-- Summarize by Plant and Pollutant - ORL Point Inventory (PTINV)
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Plant and Pollutant', 1, '
select fips, 
	plantid as plant_id, 
	plant as plant_name, 
	poll as pollutant, 
	TO_CHAR(avg(xloc), ''FM990.000009'')::double precision as longitude, 
	TO_CHAR(avg(yloc), ''FM990.000009'')::double precision as latitude, 
	max(stkhgt) as max_stack_height, 
	max(stkdiam) as max_stack_diameter, 
	max(stktemp) as max_stack_temperature, 
	max(stkflow) as max_stack_flow, 
	max(stkvel) as max_stack_velocity, 
	sum(ann_emis) as avg_annual_emissions, 
	sum(avd_emis) as avg_daily_emissions
from $TABLE[1] e
group by fips,
	plantid,
	plant,
	poll
order by fips, 
	plantid, 
	poll', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Point Inventory (PTINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Plant and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select fips, 
	plantid as plant_id, 
	plant as plant_name, 
	poll as pollutant, 
	TO_CHAR(avg(xloc), ''FM990.000009'')::double precision as longitude, 
	TO_CHAR(avg(yloc), ''FM990.000009'')::double precision as latitude, 
	max(stkhgt) as max_stack_height, 
	max(stkdiam) as max_stack_diameter, 
	max(stktemp) as max_stack_temperature, 
	max(stkflow) as max_stack_flow, 
	max(stkvel) as max_stack_velocity, 
	sum(ann_emis) as avg_annual_emissions, 
	sum(avd_emis) as avg_daily_emissions
from $TABLE[1] e
group by fips,
	plantid,
	plant,
	poll
order by fips, 
	plantid, 
	poll'
where name = 'Summarize by Plant and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Point Inventory (PTINV)'));

-- Summarize by Plant and Pollutant - ORL Merged Inventory
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Plant and Pollutant', 1, 'select fips, 
	plantid as plant_id, 
	plant as plant_name, 
	poll as pollutant, 
	sum(ann_emis) as avg_annual_emissions, 
	sum(avd_emis) as avg_daily_emissions
from $TABLE[1] e
group by fips,
	plantid,
	plant,
	poll
order by fips, 
	plantid, 
	poll', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Merged Inventory')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Plant and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select fips, 
	plantid as plant_id, 
	plant as plant_name, 
	poll as pollutant, 
	sum(ann_emis) as avg_annual_emissions, 
	sum(avd_emis) as avg_daily_emissions
from $TABLE[1] e
group by fips,
	plantid,
	plant,
	poll
order by fips, 
	plantid, 
	poll'
where name = 'Summarize by Plant and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Merged Inventory'));


-- Summarize by Control Program, U.S. State and Pollutant - Control Strategy Detailed Result
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Control Program, U.S. State and Pollutant', 1, 
'select e.control_program, 
	fips.state_name, 
	fips.state_abbr, 
	fips.fipsst, 
	e.POLL, 
	case when coalesce(sum(Emis_Reduction),0) <> 0 then sum(Annual_Cost) / sum(Emis_Reduction) else null end as avg_cost_per_ton,
	sum(e.Annual_Cost) as Annual_Cost, 
	sum(e.Inv_emissions) as Inv_emissions, 
	sum(e.Final_emissions) as Final_emissions, 
	sum(e.Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction,
	(fips.state_maxlon + fips.state_minlon) / 2 as longitude, 
	(fips.state_maxlat + fips.state_minlat) / 2 as latitude 
from $TABLE[1] e 
inner join reference.fips 
	on fips.state_county_fips = e.FIPS 
	and fips.country_num = ''0'' 
group by e.control_program, 
	fips.state_name, 
	fips.state_abbr, 
	fips.fipsst, POLL, 
	fips.state_maxlon, 
	fips.state_minlon, 
	fips.state_maxlat, 
	fips.state_minlat 
order by e.control_program, 
	fips.state_name, 
	POLL', false, 1, ''
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Control Program, U.S. State and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 
'select e.control_program, 
	fips.state_name, 
	fips.state_abbr, 
	fips.fipsst, 
	e.POLL, 
	case when coalesce(sum(Emis_Reduction),0) <> 0 then sum(Annual_Cost) / sum(Emis_Reduction) else null end as avg_cost_per_ton,
	sum(e.Annual_Cost) as Annual_Cost, 
	sum(e.Inv_emissions) as Inv_emissions, 
	sum(e.Final_emissions) as Final_emissions, 
	sum(e.Emis_Reduction) as Emis_Reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(Emis_Reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction,
	(fips.state_maxlon + fips.state_minlon) / 2 as longitude, 
	(fips.state_maxlat + fips.state_minlat) / 2 as latitude 
from $TABLE[1] e 
inner join reference.fips 
	on fips.state_county_fips = e.FIPS 
	and fips.country_num = ''0'' 
group by e.control_program, 
	fips.state_name, 
	fips.state_abbr, 
	fips.fipsst, POLL, 
	fips.state_maxlon, 
	fips.state_minlon, 
	fips.state_maxlat, 
	fips.state_minlat 
order by e.control_program, 
	fips.state_name, 
	POLL'
where name = 'Summarize by Control Program, U.S. State and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result'));


-- Summarize by Plant and Pollutant - Control Strategy Detailed Result
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Plant and Pollutant', 1, 
'select fips, 
	plantid as plant_id, 
	plant as plant_name, 
	poll as pollutant, 
	sum(inv_emissions) as Inv_emissions, 
	sum(final_emissions) as annual_emissions, 
	sum(emis_reduction) as emissions_reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(emis_reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction, 
	avg(control_eff) as avg_control_efficiency, 
	avg(rule_pen) as avg_rule_penetration, 
	avg(rule_eff) as avg_rule_effectiveness, 
	sum(annual_cost) as total_annual_cost, 
	sum(annual_oper_maint_cost) as total_op_and_maint_cost, 
	sum(annualized_capital_cost) as total_annualized_capital_cost, 
	sum(total_capital_cost) as total_capital_cost, 
	case when coalesce(sum(emis_reduction),0) <> 0 then sum(annual_cost) / sum(emis_reduction) else null end as avg_cost_per_ton,
	avg(xloc) as longitude, 
	avg(yloc) as latitude, 
	public.concatenate_with_ampersand(distinct cm_abbrev) as measures
from $TABLE[1] e
group by fips,
	plantid,
	plant,
	poll
order by fips, 
	plantid, 
	plant,
	poll', false, 1, ''
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Plant and Pollutant');

update emf.dataset_types_qa_step_templates 
set program_arguments = 
'select fips, 
	plantid as plant_id, 
	plant as plant_name, 
	poll as pollutant, 
	sum(inv_emissions) as Inv_emissions, 
	sum(final_emissions) as annual_emissions, 
	sum(emis_reduction) as emissions_reduction, 
	case when coalesce(sum(Inv_emissions),0) <> 0 then sum(emis_reduction) / sum(Inv_emissions) * 100 else null end as percent_reduction, 
	avg(control_eff) as avg_control_efficiency, 
	avg(rule_pen) as avg_rule_penetration, 
	avg(rule_eff) as avg_rule_effectiveness, 
	sum(annual_cost) as total_annual_cost, 
	sum(annual_oper_maint_cost) as total_op_and_maint_cost, 
	sum(annualized_capital_cost) as total_annualized_capital_cost, 
	sum(total_capital_cost) as total_capital_cost, 
	case when coalesce(sum(emis_reduction),0) <> 0 then sum(annual_cost) / sum(emis_reduction) else null end as avg_cost_per_ton,
	avg(xloc) as longitude, 
	avg(yloc) as latitude, 
	public.concatenate_with_ampersand(distinct cm_abbrev) as measures
from $TABLE[1] e
group by fips,
	plantid,
	plant,
	poll
order by fips, 
	plantid, 
	plant,
	poll'
where name = 'Summarize by Plant and Pollutant'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result'));

-- Summarize all Control Measures - Control Strategy Detailed Result
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize all Control Measures', 1, 'select cm.name as CMName, 
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
order by cm.name, p.name', false, 1, ''
from emf.dataset_types dt
where name in ('Control Strategy Detailed Result')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize all Control Measures');

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

-- Compare VOC Speciation with HAP inventory - Details - ORL Point Inventory (PTINV)
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Compare VOC Speciation with HAP inventory - Details', (select id from emf.qa_programs where name = 'Compare VOC Speciation with HAP inventory') as program_id, E'-summaryType\nDetails', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Point Inventory (PTINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Compare VOC Speciation with HAP inventory - Details');

-- Compare VOC Speciation with HAP inventory - By MACT - ORL Point Inventory (PTINV)
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Compare VOC Speciation with HAP inventory - By MACT', (select id from emf.qa_programs where name = 'Compare VOC Speciation with HAP inventory') as program_id, E'-summaryType\nBy MACT', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Point Inventory (PTINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Compare VOC Speciation with HAP inventory - By MACT');

-- Compare VOC Speciation with HAP inventory - Details - ORL Point Inventory (PTINV)
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Compare VOC Speciation with HAP inventory - By NAICS', (select id from emf.qa_programs where name = 'Compare VOC Speciation with HAP inventory') as program_id, E'-summaryType\nBy NAICS', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Point Inventory (PTINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Compare VOC Speciation with HAP inventory - By NAICS');

-- Compare VOC Speciation with HAP inventory - By Profile Code - ORL Point Inventory (PTINV)
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Compare VOC Speciation with HAP inventory - By Profile Code', (select id from emf.qa_programs where name = 'Compare VOC Speciation with HAP inventory') as program_id, E'-summaryType\nBy Profile Code', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Point Inventory (PTINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Compare VOC Speciation with HAP inventory - By Profile Code');

-- Compare VOC Speciation with HAP inventory - By SIC - ORL Point Inventory (PTINV)
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Compare VOC Speciation with HAP inventory - By SIC', (select id from emf.qa_programs where name = 'Compare VOC Speciation with HAP inventory') as program_id, E'-summaryType\nBy SIC', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Point Inventory (PTINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Compare VOC Speciation with HAP inventory - By SIC');

-- Compare VOC Speciation with HAP inventory - By SCC - ORL Point Inventory (PTINV)
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Compare VOC Speciation with HAP inventory - By SCC', (select id from emf.qa_programs where name = 'Compare VOC Speciation with HAP inventory') as program_id, E'-summaryType\nBy SCC', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Point Inventory (PTINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Compare VOC Speciation with HAP inventory - By SCC');

-- Compare VOC Speciation with HAP inventory - By NEI Unique Id - ORL Point Inventory (PTINV)
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Compare VOC Speciation with HAP inventory - By NEI Unique Id', (select id from emf.qa_programs where name = 'Compare VOC Speciation with HAP inventory') as program_id, E'-summaryType\nBy NEI Unique Id', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Point Inventory (PTINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Compare VOC Speciation with HAP inventory - By NEI Unique Id');

-- Compare VOC Speciation with HAP inventory - Details - ORL Nonpoint Inventory (ARINV)
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Compare VOC Speciation with HAP inventory - Details', (select id from emf.qa_programs where name = 'Compare VOC Speciation with HAP inventory') as program_id, E'-summaryType\nDetails', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Compare VOC Speciation with HAP inventory - Details');

-- Compare VOC Speciation with HAP inventory - By MACT - ORL Nonpoint Inventory (ARINV)
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Compare VOC Speciation with HAP inventory - By MACT', (select id from emf.qa_programs where name = 'Compare VOC Speciation with HAP inventory') as program_id, E'-summaryType\nBy MACT', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Compare VOC Speciation with HAP inventory - By MACT');

-- Compare VOC Speciation with HAP inventory - Details - ORL Nonpoint Inventory (ARINV)
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Compare VOC Speciation with HAP inventory - By NAICS', (select id from emf.qa_programs where name = 'Compare VOC Speciation with HAP inventory') as program_id, E'-summaryType\nBy NAICS', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Compare VOC Speciation with HAP inventory - By NAICS');

-- Compare VOC Speciation with HAP inventory - By Profile Code - ORL Nonpoint Inventory (ARINV)
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Compare VOC Speciation with HAP inventory - By Profile Code', (select id from emf.qa_programs where name = 'Compare VOC Speciation with HAP inventory') as program_id, E'-summaryType\nBy Profile Code', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Compare VOC Speciation with HAP inventory - By Profile Code');

-- Compare VOC Speciation with HAP inventory - By SIC - ORL Nonpoint Inventory (ARINV)
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Compare VOC Speciation with HAP inventory - By SIC', (select id from emf.qa_programs where name = 'Compare VOC Speciation with HAP inventory') as program_id, E'-summaryType\nBy SIC', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Compare VOC Speciation with HAP inventory - By SIC');

-- Compare VOC Speciation with HAP inventory - By SCC - ORL Nonpoint Inventory (ARINV)
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Compare VOC Speciation with HAP inventory - By SCC', (select id from emf.qa_programs where name = 'Compare VOC Speciation with HAP inventory') as program_id, E'-summaryType\nBy SCC', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Compare VOC Speciation with HAP inventory - By SCC');

-- Summarize by PM CEFF Differences - ORL Point Inventory (PTINV)
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by PM CEFF Differences', 1, 'select a.fips,
	a.scc, 
	a.plantid, 
	a.pointid, 
	a.stackid, 
	a.segment, 
	a.poll, 
	a.ceff
FROM $TABLE[1] a

	full join $TABLE[1] b
	on b.fips = a.fips
	and b.scc = a.scc

	and b.plantid = a.plantid
	and b.pointid = a.pointid
	and b.stackid = a.stackid
	and b.segment = a.segment
where b.poll in (''PM2_5'',''PM10'')
	and a.poll in (''PM2_5'',''PM10'')
	and (
		(b.ceff is null and a.ceff is not null)
		or (b.ceff is not null and a.ceff is null)
	)
order by a.fips,
	a.scc, 
	a.plantid, 
	a.pointid, 
	a.stackid, 
	a.segment, 
	a.poll', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Point Inventory (PTINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by PM CEFF Differences');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select a.fips,
	a.scc, 
	a.plantid, 
	a.pointid, 
	a.stackid, 
	a.segment, 
	a.poll, 
	a.ceff
FROM $TABLE[1] a

	full join $TABLE[1] b
	on b.fips = a.fips
	and b.scc = a.scc

	and b.plantid = a.plantid
	and b.pointid = a.pointid
	and b.stackid = a.stackid
	and b.segment = a.segment
where b.poll in (''PM2_5'',''PM10'')
	and a.poll in (''PM2_5'',''PM10'')
	and (
		(b.ceff is null and a.ceff is not null)
		or (b.ceff is not null and a.ceff is null)
	)
order by a.fips,
	a.scc, 
	a.plantid, 
	a.pointid, 
	a.stackid, 
	a.segment, 
	a.poll
'
where name = 'Summarize by PM CEFF Differences'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Point Inventory (PTINV)'));

-- Summarize by Missing PM CEFF - 'ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)'
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'Summarize by Missing PM CEFF', 1, 'select a.fips,
	a.scc, 
	a.poll, 
	a.ceff
FROM $TABLE[1] a

	full join $TABLE[1] b
	on b.fips = a.fips
	and b.scc = a.scc

where b.poll in (''PM2_5'',''PM10'')
	and a.poll in (''PM2_5'',''PM10'')
	and (
		(b.ceff is null and a.ceff is not null)
		or (b.ceff is not null and a.ceff is null)
	)
order by a.fips,
	a.scc, 
	a.poll', false, 1, ''
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'Summarize by Missing PM CEFF');

update emf.dataset_types_qa_step_templates 
set program_arguments = 'select a.fips,
	a.scc, 
	a.poll, 
	a.ceff
FROM $TABLE[1] a

	full join $TABLE[1] b
	on b.fips = a.fips
	and b.scc = a.scc

where b.poll in (''PM2_5'',''PM10'')
	and a.poll in (''PM2_5'',''PM10'')
	and (
		(b.ceff is null and a.ceff is not null)
		or (b.ceff is not null and a.ceff is null)
	)
order by a.fips,
	a.scc, 
	a.poll
'
where name = 'Summarize by Missing PM CEFF'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)'));

-- Template for QA Program "SMOKE output annual state summaries crosstab" - 'Control Strategy Detailed Result','ORL Nonpoint Inventory (ARINV)','ORL Nonroad Inventory (ARINV)','ORL Onroad Inventory (MBINV)'
insert into emf.dataset_types_qa_step_templates (dataset_type_id, list_index, name, qa_program_id, program_arguments, required, order_no, description)
select dt.id, (select COALESCE(max(list_index) + 1, 0) from emf.dataset_types_qa_step_templates where dataset_type_id = dt.id) as list_index, 'SMOKE output annual state summaries crosstab', (select id from emf.qa_programs where name = 'SMOKE output annual state summaries crosstab'), 
'-polllist
CO
NOX
VOC
SO2
NH3
PM10
PM2_5
CHLORINE
HCL', false, 1, ''
from emf.dataset_types dt
where name in ('Smkmerge report state annual summary (CSV)')
	and not exists (select 1 from emf.dataset_types_qa_step_templates qatemp where qatemp.dataset_type_id = dt.id and qatemp.name = 'SMOKE output annual state summaries crosstab');

update emf.dataset_types_qa_step_templates 
set program_arguments = '-polllist
CO
NOX
VOC
SO2
NH3
PM10
PM2_5
CHLORINE
HCL'
where name = 'SMOKE output annual state summaries crosstab'
and dataset_type_id in 
(select id 
from emf.dataset_types dt
where name in ('Smkmerge report state annual summary (CSV)'));

INSERT into emf.properties values(DEFAULT, 'schema_sms_version', '1');

