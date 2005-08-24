--- Table: emf.datasets

-- DROP TABLE emf.datasets;

CREATE TABLE emf.datasets
(
  name varchar(128) NOT NULL,
  creator varchar(128) NOT NULL,
  datasettype varchar(255) NOT NULL,
  units varchar(255) NOT NULL,
  region varchar(255) NOT NULL,
  "year" int4 NOT NULL DEFAULT 0,
  country varchar(255) NOT NULL,
  temporal_resolution varchar(255) NOT NULL,
  start_date_time timestamp NOT NULL,
  stop_date_time timestamp NOT NULL,
  description varchar(255) NOT NULL,
  dataset_id int4 NOT NULL
) 
WITHOUT OIDS;
ALTER TABLE emf.datasets OWNER TO emf;
