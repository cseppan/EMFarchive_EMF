
-- added (12/28/2009) to add creator full name to datasets
ALTER TABLE emf.datasets ADD COLUMN creator_full_name varchar(255) DEFAULT '';
UPDATE emf.datasets as ds SET creator_full_name = (SELECT name FROM	emf.users where username = ds.creator);

-- added (01/25/2010) to add creator full name to datasets
ALTER TABLE emf.users ADD COLUMN is_want_emails boolean NOT NULL DEFAULT true;
ALTER TABLE emf.users ADD COLUMN last_login_date timestamp;