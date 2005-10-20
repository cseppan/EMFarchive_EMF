DROP TABLE internal_sources;

CREATE TABLE internal_sources
(
  dataset_id int4 NOT NULL,
  list_index int4,
  data_source varchar(255) NOT NULL,
  table_name varchar(255) NOT NULL,
  table_type varchar(255) NOT NULL,
  table_columns text NOT NULL,
  file_size int4 NOT NULL,
  CONSTRAINT internalsources_to_datasets FOREIGN KEY (dataset_id) REFERENCES datasets (dataset_id) ON UPDATE CASCADE ON DELETE CASCADE
) 
WITHOUT OIDS;
ALTER TABLE internal_sources OWNER TO emf;
