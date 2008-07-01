CREATE OR REPLACE FUNCTION public.populate_sources_table(
	inv_table_name varchar(63),
	inv_filter varchar
) RETURNS void AS $$
DECLARE
	is_point_table boolean := false;
BEGIN
	-- see if there are point columns 
	SELECT count(1) = 4
	FROM pg_class c
		inner join pg_attribute a
		on a.attrelid = c.oid
		inner join pg_type t
		on t.oid = a.atttypid
	WHERE c.relname = lower(inv_table_name)
		and a.attname in ('plantid','pointid','stackid','segment')
		AND a.attnum > 0
	into is_point_table;

	execute 'insert into emf.sources (scc, fips ' || case when is_point_table = false then '' else ', plantid, pointid, stackid, segment' end || ', source)
		select distinct on (inv.scc, inv.fips ' || case when is_point_table = false then '' else ', inv.plantid, inv.pointid, inv.stackid, inv.segment' end || ')
		inv.scc, inv.fips ' || case when is_point_table = false then '' else ', inv.plantid, inv.pointid, inv.stackid, inv.segment' end || ',
		inv.scc || inv.fips || ' || case when is_point_table = false then 'rpad('''', 60)' else 'rpad(coalesce(inv.plantid, ''''), 15) || rpad(coalesce(inv.pointid, ''''), 15) || rpad(coalesce(inv.stackid, ''''), 15) || rpad(coalesce(inv.segment, ''''), 15)' end || '
	FROM emissions.' || inv_table_name || ' inv
		left outer join emf.sources
		on sources.scc = inv.scc
		and sources.fips = inv.fips
		' || case when is_point_table = false then '
		and sources.plantid is null
		and sources.pointid is null
		and sources.stackid is null
		and sources.segment is null
		' else '
		and sources.plantid = inv.plantid
		and sources.pointid = inv.pointid
		and sources.stackid = inv.stackid
		and sources.segment = inv.segment
		' end || '
	where 	sources.id is null ' || coalesce(replace(replace(replace(replace(replace(replace(lower(inv_filter), 'scc', 'inv.scc'), 'fips', 'inv.fips'), 'plantid', 'inv.plantid'),  'pointid', 'inv.pointid'), 'stackid', 'inv.stackid'), 'segment', 'inv.segment'), '') || '
		';

	return;
END;
$$ LANGUAGE plpgsql;
