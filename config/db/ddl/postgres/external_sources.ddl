DROP TABLE external_sources;

CREATE TABLE external_sources
(
  dataset_id int4 NOT NULL,
  list_index int4,
  data_source varchar(255) NOT NULL,
  CONSTRAINT dataset_tables_to_datasets FOREIGN KEY (dataset_id) REFERENCES datasets (dataset_id) ON UPDATE CASCADE ON DELETE CASCADE
) 
WITHOUT OIDS;
ALTER TABLE external_sources OWNER TO emf;

