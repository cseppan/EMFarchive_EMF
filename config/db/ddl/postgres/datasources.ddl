-- Table: datasources

-- DROP TABLE datasources;

CREATE TABLE datasources
(
  ds_id serial UNIQUE NOT NULL,
  dataset_id int4 NOT NULL,
  data_source varchar(255) NOT NULL,
  CONSTRAINT datasources_to_datasets FOREIGN KEY (dataset_id) REFERENCES datasets (dataset_id)
) 
WITHOUT OIDS;
ALTER TABLE datasources OWNER TO emf;