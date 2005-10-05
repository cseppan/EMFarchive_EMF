CREATE TABLE emf.datasettypes
(
  dataset_type_id serial UNIQUE NOT NULL,
  name varchar(255) NOT NULL,
  description text NOT NULL,
  minfiles integer NOT NULL DEFAULT 0,
  maxfiles integer NOT NULL DEFAULT 0,
  min_columns integer NOT NULL DEFAULT 0,
  max_columns integer NOT NULL DEFAULT 0
) 
WITHOUT OIDS;
ALTER TABLE emf.datasettypes OWNER TO emf;