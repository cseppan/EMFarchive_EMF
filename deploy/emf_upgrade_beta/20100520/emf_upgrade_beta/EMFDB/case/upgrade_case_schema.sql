-- 4/21/2010 update job uniqueness criterion
ALTER TABLE cases.cases_casejobs DROP CONSTRAINT  cases_casejobs_case_id_key;
ALTER TABLE cases.cases_casejobs ADD CONSTRAINT  cases_casejobs_case_id_key  UNIQUE (case_id, name, sector_id, region_id);
