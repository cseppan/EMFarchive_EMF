CREATE TABLE datasettables
(
  dataset_id int4 NOT NULL,
  table_type varchar(255) NOT NULL,
  table_name varchar(255) NOT NULL,
  CONSTRAINT dataset_tables_to_datasets FOREIGN KEY (dataset_id)
      REFERENCES datasets (dataset_id)
) 
WITHOUT OIDS;
ALTER TABLE datasettables OWNER TO emf;