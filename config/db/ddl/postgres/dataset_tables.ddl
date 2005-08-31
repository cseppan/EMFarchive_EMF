DROP TABLE datasettables;

CREATE TABLE datasettables
(
  dataset_id int4 NOT NULL,
  table_type varchar(255) NOT NULL,
  table_name varchar(255) NOT NULL
) 
WITHOUT OIDS;
ALTER TABLE datasettables OWNER TO emf;

