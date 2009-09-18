--added (8/21) to allow locking of georegions for edit
ALTER TABLE emf.georegions ADD COLUMN lock_owner varchar(255);
ALTER TABLE emf.georegions ADD COLUMN lock_date timestamp;
