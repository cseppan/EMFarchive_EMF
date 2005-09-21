CREATE TABLE emf_properties
(
  prop_id serial unique NOT NULL,
  prop_name varchar(255) NOT NULL,
  prop_value varchar(255) NOT NULL
) 
WITHOUT OIDS;
ALTER TABLE emf_properties OWNER TO emf;