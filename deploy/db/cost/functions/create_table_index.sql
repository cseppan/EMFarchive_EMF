CREATE OR REPLACE FUNCTION public.create_table_index(table_name character varying, table_col_list character varying, index_name_prefix character varying)
  RETURNS void AS
$BODY$
DECLARE
	index_name varchar(63) := '';
	has_columns boolean := false;
BEGIN

	-- see if there are columns to be indexed
	has_columns := public.check_table_for_columns(table_name, table_col_list, ',');

	-- Create Indexes....

	IF has_columns THEN
	
		-- create record_id btree index
		IF length(index_name_prefix || '_' || table_name) >= 63 - length(index_name_prefix) THEN
			index_name := index_name_prefix || '_' || substr(table_name, length(index_name_prefix) + 1, 63);
		ELSE
			index_name := index_name_prefix || '_' || table_name;
		END IF;
		execute 'CREATE INDEX ' || index_name || '
				ON emissions.' || table_name || '
				USING btree
				(' || table_col_list || ')';
	END IF;
END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION public.create_table_index(table_name character varying, table_col_list character varying, index_name_prefix character varying) OWNER TO emf;