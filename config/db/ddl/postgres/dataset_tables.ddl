DROP TABLE datasettables;

CREATE TABLE datasettables
(
  dataset_tables_id serial NOT NULL,
  table_type varchar(255) NOT NULL,
  table_name varchar(255) NOT NULL,
  CONSTRAINT datasettables_dataset_tables_id_key UNIQUE (dataset_tables_id)
) 
WITHOUT OIDS;
ALTER TABLE datasettables OWNER TO emf;
