-- Function: get_least_cost_worksheet_emis_reduction(character varying, character varying, integer)

-- DROP FUNCTION get_least_cost_worksheet_emis_reduction(character varying, character varying, integer);

CREATE OR REPLACE FUNCTION public.get_least_cost_worksheet_emis_reduction(worksheet_table_name character varying, target_pollutant character varying, target_record_offset integer)
  RETURNS double precision AS
$BODY$
DECLARE
	emis_reduction double precision;
BEGIN

	execute 'SELECT sum(emis_reduction)
	from (
		SELECT distinct on (source) emis_reduction
		from (
			SELECT emis_reduction, marginal, record_id, source
			FROM emissions.' || worksheet_table_name || '
			where status is null 
				and poll = ' || quote_literal(target_pollutant) || '
			ORDER BY marginal, emis_reduction desc, record_id
			limit ' || target_record_offset || '
		) tbl
		ORDER BY source, marginal desc, emis_reduction, record_id desc
	) tbl' 
	into emis_reduction;

	RETURN coalesce(emis_reduction,0.0);
END;
$BODY$
  LANGUAGE 'plpgsql' IMMUTABLE
  COST 100;
ALTER FUNCTION get_least_cost_worksheet_emis_reduction(character varying, character varying, integer) OWNER TO postgres;
