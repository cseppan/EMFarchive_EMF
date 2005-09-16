CREATE TABLE dataset_access_logs
(
  dataset_id int4 NOT NULL,
  username varchar(255) NOT NULL,
  date timestamp NOT NULL,
  version varchar(128) NOT NULL,
  description text NOT NULL,
  "location" varchar(255) NOT NULL
) 
WITHOUT OIDS;
ALTER TABLE dataset_access_logs OWNER TO emf;
