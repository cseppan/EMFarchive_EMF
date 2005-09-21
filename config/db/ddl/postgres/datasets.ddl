--- Table: emf.datasets
DROP TABLE datasets CASCADE;
DELETE FROM DATASET_ACCESS_LOGS;
DELETE FROM DATASETTABLES;

CREATE TABLE datasets
(
  dataset_id serial NOT NULL,
  name varchar(255) NOT NULL,
  creator varchar(255) NOT NULL,
  status varchar(255) NOT NULL,
  datasettype varchar(255) NOT NULL,
  units varchar(255),
  region varchar(255),
  "year" int4 NOT NULL DEFAULT 0,
  country varchar(255),
  temporal_resolution varchar(255),
  start_date_time timestamp,
  stop_date_time timestamp,
  description text,
  sector varchar(255),
  project varchar(255),
  created_date_time timestamp,
  modified_date_time timestamp,
  accessed_time timestamp,
  CONSTRAINT datasets_pkey PRIMARY KEY (dataset_id),
  CONSTRAINT datasets_name_key UNIQUE (name)
) 
WITHOUT OIDS;
ALTER TABLE datasets OWNER TO emf;
