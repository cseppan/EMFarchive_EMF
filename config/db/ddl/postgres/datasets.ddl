--- Table: emf.datasets
CREATE TABLE "emf"."datasets"
(
  dataset_id serial not null unique,
  name varchar(128) NOT NULL,
  creator varchar(128) NOT NULL,
  datasettype varchar(255) NOT NULL,
  units varchar(255),
  region varchar(255),
  "year" integer NOT NULL DEFAULT 0,
  country varchar(255) ,
  temporal_resolution varchar(255),
  start_date_time timestamp,
  stop_date_time timestamp,
  description varchar(255),
  PRIMARY KEY("dataset_id")
) 
WITHOUT OIDS;
ALTER TABLE emf.datasets OWNER TO emf;
