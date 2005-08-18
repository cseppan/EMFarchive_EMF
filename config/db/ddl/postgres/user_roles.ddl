CREATE TABLE emf.user_roles
(
  user_name varchar(15) NOT NULL,
  role_name varchar(15) NOT NULL,
  CONSTRAINT pv_user_roles PRIMARY KEY (user_name, role_name)
) 
WITH OIDS;
ALTER TABLE emf.user_roles OWNER TO emf;