CREATE OR REPLACE FUNCTION public.clean_project_future_year_inventory_control_programs(
	control_strategy_id integer) RETURNS void AS
$BODY$
DECLARE
	control_program RECORD;
	count integer := 0;
BEGIN

	-- clean that all inputs look fine, before actually running the projection.

  	FOR control_program IN EXECUTE 
		'select cp."name" as control_program_name, cpt."name" as type, lower(i.table_name) as table_name, 
			cp.start_date, cp.end_date, 
			cp.dataset_id, cp.dataset_version, 
			dt."name" as dataset_type
		 from emf.control_strategy_programs csp

			inner join emf.control_programs cp
			on cp.id = csp.control_program_id

			inner join emf.control_program_types cpt
			on cpt.id = cp.control_program_type_id

			inner join emf.internal_sources i
			on i.dataset_id = cp.dataset_id

			inner join emf.datasets d
			on d.id = i.dataset_id

			inner join emf.dataset_types dt
			on dt.id = d.dataset_type
		where csp.control_strategy_id = ' || control_strategy_id || '
		order by processing_order'
	LOOP
		-- make sure the fips code is only 5 characters, not 6 characters, strip last character if necessary
		execute 
			'update emissions.' || control_program.table_name || '
			set fips = substring(fips,2,5)
			where length(fips) = 6;';

		-- look at the closure control program inputs and table format (make sure all the right columns are in the table)
		IF control_program.type = 'Control' and control_program.dataset_type = 'Control Packet' THEN

			execute 
				'update emissions.' || control_program.table_name || '
				set plantid = case when plantid is null or trim(plantid) = ''0'' or trim(plantid) = ''-9'' or trim(plantid) = '''' then null::character varying(15) else plantid end,
					pointid = case when pointid is null or trim(pointid) = ''0'' or trim(pointid) = ''-9'' or trim(pointid) = '''' then null::character varying(15) else pointid end,
					stackid = case when stackid is null or trim(stackid) = ''0'' or trim(stackid) = ''-9'' or trim(stackid) = '''' then null::character varying(15) else stackid end,
					segment = case when segment is null or trim(segment) = ''0'' or trim(segment) = ''-9'' or trim(segment) = '''' then null::character varying(15) else segment end,
					fips = case when fips is null or trim(fips) = ''0'' or trim(fips) = ''-9'' or trim(fips) = '''' then null::character varying(6) else fips end,
					scc = case when scc is null or trim(scc) = ''0'' or trim(scc) = ''-9'' or trim(scc) = '''' then null::character varying(10) else scc end,
					poll = case when poll is null or trim(poll) = ''0'' or trim(poll) = ''-9'' or trim(poll) = '''' then null::character varying(16) else poll end,
					mact = case when mact is null or trim(mact) = ''0'' or trim(mact) = ''-9'' or trim(mact) = '''' then null::character varying(6) else mact end,
					sic = case when sic is null or trim(sic) = ''0'' or trim(sic) = ''-9'' or trim(sic) = '''' then null::character varying(4) else sic end,
					naics = case when naics is null or trim(naics) = ''0'' or trim(naics) = ''-9'' or trim(naics) = '''' then null::character varying(6) else naics end,
					pri_cm_abbrev = case when pri_cm_abbrev is null or trim(pri_cm_abbrev) = ''0'' or trim(pri_cm_abbrev) = ''-9'' or trim(pri_cm_abbrev) = '''' then null::character varying(4) else pri_cm_abbrev end 
				where trim(plantid) in (''0'',''-9'','''')
					or trim(pointid) in (''0'',''-9'','''')
					or trim(stackid) in (''0'',''-9'','''')
					or trim(segment) in (''0'',''-9'','''')
					or trim(fips) in (''0'',''-9'','''')
					or trim(scc) in (''0'',''-9'','''')
					or trim(poll) in (''0'',''-9'','''')
					or trim(mact) in (''0'',''-9'','''')
					or trim(sic) in (''0'',''-9'','''') 
					or trim(naics) in (''0'',''-9'','''') 
					or trim(pri_cm_abbrev) in (''0'',''-9'','''');';

/*

update emissions.ds_alm_projections_2_v3_974434595
set plantid = case when plantid is null or trim(plantid) = '0' or trim(plantid) = '-9' or trim(plantid) = '' then null::character varying(15) else plantid end,
	pointid = case when pointid is null or trim(pointid) = '0' or trim(pointid) = '-9' or trim(pointid) = '' then null::character varying(15) else pointid end,
	stackid = case when stackid is null or trim(stackid) = '0' or trim(stackid) = '-9' or trim(stackid) = '' then null::character varying(15) else stackid end,
	segment = case when segment is null or trim(segment) = '0' or trim(segment) = '-9' or trim(segment) = '' then null::character varying(15) else segment end,
	fips = case when fips is null or trim(fips) = '0' or trim(fips) = '-9' or trim(fips) = '' then null::character varying(6) else fips end,
	scc = case when scc is null or trim(scc) = '0' or trim(scc) = '-9' or trim(scc) = '' then null::character varying(10) else scc end,
	mact = case when mact is null or trim(mact) = '0' or trim(mact) = '-9' or trim(mact) = '' then null::character varying(6) else mact end,
	sic = case when sic is null or trim(sic) = '0' or trim(sic) = '-9' or trim(sic) = '' then null::character varying(4) else sic end 
where trim(plantid) in ('0','-9','')
	or trim(pointid) in ('0','-9','')
	or trim(stackid) in ('0','-9','')
	or trim(segment) in ('0','-9','')
	or trim(fips) in ('0','-9','')
	or trim(scc) in ('0','-9','')
	or trim(mact) in ('0','-9','')
	or trim(sic) in ('0','-9','');
vacuum analyze emissions.ds_alm_projections_2_v3_974434595;


update emissions.ds_control_ptnonipm_2020ce_04apr2008_v2_782987161
set plantid = case when plantid is null or trim(plantid) = '0' or trim(plantid) = '-9' or trim(plantid) = '' then null::character varying(15) else plantid end,
	pointid = case when pointid is null or trim(pointid) = '0' or trim(pointid) = '-9' or trim(pointid) = '' then null::character varying(15) else pointid end,
	stackid = case when stackid is null or trim(stackid) = '0' or trim(stackid) = '-9' or trim(stackid) = '' then null::character varying(15) else stackid end,
	segment = case when segment is null or trim(segment) = '0' or trim(segment) = '-9' or trim(segment) = '' then null::character varying(15) else segment end,
	fips = case when fips is null or trim(fips) = '0' or trim(fips) = '-9' or trim(fips) = '' then null::character varying(6) else fips end,
	scc = case when scc is null or trim(scc) = '0' or trim(scc) = '-9' or trim(scc) = '' then null::character varying(10) else scc end,
	mact = case when mact is null or trim(mact) = '0' or trim(mact) = '-9' or trim(mact) = '' then null::character varying(6) else mact end,
	sic = case when sic is null or trim(sic) = '0' or trim(sic) = '-9' or trim(sic) = '' then null::character varying(4) else sic end,
	pri_cm_abbrev = case when pri_cm_abbrev is null or trim(pri_cm_abbrev) = '0' or trim(pri_cm_abbrev) = '-9' or trim(pri_cm_abbrev) = '' then null::character varying(4) else pri_cm_abbrev end 
where trim(plantid) in ('0','-9','')
	or trim(pointid) in ('0','-9','')
	or trim(stackid) in ('0','-9','')
	or trim(segment) in ('0','-9','')
	or trim(fips) in ('0','-9','')
	or trim(scc) in ('0','-9','')
	or trim(mact) in ('0','-9','')
	or trim(sic) in ('0','-9','')
	or trim(pri_cm_abbrev) in ('0','-9','');
vacuum analyze emissions.ds_control_ptnonipm_2020ce_04apr2008_v2_782987161;

update emissions.ds_deletions_2005_1050880615
set plantid = case when plantid is null or trim(plantid) = '0' or trim(plantid) = '-9' or trim(plantid) = '' then null::character varying(15) else plantid end,
	pointid = case when pointid is null or trim(pointid) = '0' or trim(pointid) = '-9' or trim(pointid) = '' then null::character varying(15) else pointid end,
	stackid = case when stackid is null or trim(stackid) = '0' or trim(stackid) = '-9' or trim(stackid) = '' then null::character varying(15) else stackid end,
	segment = case when segment is null or trim(segment) = '0' or trim(segment) = '-9' or trim(segment) = '' then null::character varying(15) else segment end,
	fips = case when fips is null or trim(fips) = '0' or trim(fips) = '-9' or trim(fips) = '' then null::character varying(6) else fips end 
where trim(plantid) in ('0','-9','')
	or trim(pointid) in ('0','-9','')
	or trim(stackid) in ('0','-9','')
	or trim(segment) in ('0','-9','')
	or trim(fips) in ('0','-9','');
vacuum analyze emissions.ds_deletions_2005_1050880615;

*/

		-- look at the projection control program table format (make sure all the right columns are in the table)
		ELSIF control_program.type = 'Projection' and control_program.dataset_type = 'Projection Packet' THEN

			execute 
				'update emissions.' || control_program.table_name || '
				set plantid = case when plantid is null or trim(plantid) = ''0'' or trim(plantid) = ''-9'' or trim(plantid) = '''' then null::character varying(15) else plantid end,
					pointid = case when pointid is null or trim(pointid) = ''0'' or trim(pointid) = ''-9'' or trim(pointid) = '''' then null::character varying(15) else pointid end,
					stackid = case when stackid is null or trim(stackid) = ''0'' or trim(stackid) = ''-9'' or trim(stackid) = '''' then null::character varying(15) else stackid end,
					segment = case when segment is null or trim(segment) = ''0'' or trim(segment) = ''-9'' or trim(segment) = '''' then null::character varying(15) else segment end,
					fips = case when fips is null or trim(fips) = ''0'' or trim(fips) = ''-9'' or trim(fips) = '''' then null::character varying(6) else fips end,
					scc = case when scc is null or trim(scc) = ''0'' or trim(scc) = ''-9'' or trim(scc) = '''' then null::character varying(10) else scc end,
					poll = case when poll is null or trim(poll) = ''0'' or trim(poll) = ''-9'' or trim(poll) = '''' then null::character varying(16) else poll end,
					mact = case when mact is null or trim(mact) = ''0'' or trim(mact) = ''-9'' or trim(mact) = '''' then null::character varying(6) else mact end,
					sic = case when sic is null or trim(sic) = ''0'' or trim(sic) = ''-9'' or trim(sic) = '''' then null::character varying(4) else sic end,
					naics = case when naics is null or trim(naics) = ''0'' or trim(naics) = ''-9'' or trim(naics) = '''' then null::character varying(6) else naics end
				where trim(plantid) in (''0'',''-9'','''')
					or trim(pointid) in (''0'',''-9'','''')
					or trim(stackid) in (''0'',''-9'','''')
					or trim(segment) in (''0'',''-9'','''')
					or trim(fips) in (''0'',''-9'','''')
					or trim(scc) in (''0'',''-9'','''')
					or trim(poll) in (''0'',''-9'','''')
					or trim(mact) in (''0'',''-9'','''')
					or trim(sic) in (''0'',''-9'','''')
					or trim(naics) in (''0'',''-9'','''');';


		-- look at the cap control program table format (make sure all the right columns are in the table)
		ELSIF control_program.type = 'Allowable' THEN --and control_program.dataset_type = 'Projection Packet' THEN

			execute 
				'update emissions.' || control_program.table_name || '
				set plantid = case when plantid is null or trim(plantid) = ''0'' or trim(plantid) = ''-9'' or trim(plantid) = '''' then null::character varying(15) else plantid end,
					pointid = case when pointid is null or trim(pointid) = ''0'' or trim(pointid) = ''-9'' or trim(pointid) = '''' then null::character varying(15) else pointid end,
					stackid = case when stackid is null or trim(stackid) = ''0'' or trim(stackid) = ''-9'' or trim(stackid) = '''' then null::character varying(15) else stackid end,
					segment = case when segment is null or trim(segment) = ''0'' or trim(segment) = ''-9'' or trim(segment) = '''' then null::character varying(15) else segment end,
					fips = case when fips is null or trim(fips) = ''0'' or trim(fips) = ''-9'' or trim(fips) = '''' then null::character varying(6) else fips end,
					scc = case when scc is null or trim(scc) = ''0'' or trim(scc) = ''-9'' or trim(scc) = '''' then null::character varying(10) else scc end,
					poll = case when poll is null or trim(poll) = ''0'' or trim(poll) = ''-9'' or trim(poll) = '''' then null::character varying(16) else poll end,
--					mact = case when mact is null or trim(mact) = ''0'' or trim(mact) = ''-9'' or trim(mact) = '''' then null::character varying(6) else mact end,
					sic = case when sic is null or trim(sic) = ''0'' or trim(sic) = ''-9'' or trim(sic) = '''' then null::character varying(4) else sic end,
					naics = case when naics is null or trim(naics) = ''0'' or trim(naics) = ''-9'' or trim(naics) = '''' then null::character varying(6) else naics end
				where trim(plantid) in (''0'',''-9'','''')
					or trim(pointid) in (''0'',''-9'','''')
					or trim(stackid) in (''0'',''-9'','''')
					or trim(segment) in (''0'',''-9'','''')
					or trim(fips) in (''0'',''-9'','''')
					or trim(scc) in (''0'',''-9'','''')
					or trim(poll) in (''0'',''-9'','''')
--					or trim(mact) in (''0'',''-9'','''')
					or trim(sic) in (''0'',''-9'','''');';


		-- look at the projection control program table format (make sure all the right columns are in the table)
		ELSIF control_program.type = 'Plant Closure' and control_program.dataset_type = 'Plant Closure (CSV)' THEN

			execute 
				'update emissions.' || control_program.table_name || '
				set plantid = case when plantid is null or trim(plantid) = ''0'' or trim(plantid) = ''-9'' or trim(plantid) = '''' then null::character varying(15) else plantid end,
					pointid = case when pointid is null or trim(pointid) = ''0'' or trim(pointid) = ''-9'' or trim(pointid) = '''' then null::character varying(15) else pointid end,
					stackid = case when stackid is null or trim(stackid) = ''0'' or trim(stackid) = ''-9'' or trim(stackid) = '''' then null::character varying(15) else stackid end,
					segment = case when segment is null or trim(segment) = ''0'' or trim(segment) = ''-9'' or trim(segment) = '''' then null::character varying(15) else segment end,
					fips = case when fips is null or trim(fips) = ''0'' or trim(fips) = ''-9'' or trim(fips) = '''' then null::character varying(6) else fips end,
					effective_date = case when effective_date is null or trim(effective_date) = ''0'' or trim(effective_date) = ''-9'' or trim(effective_date) = '''' then null::character varying(10) else effective_date end 
				where trim(plantid) in (''0'',''-9'','''')
					or trim(pointid) in (''0'',''-9'','''')
					or trim(stackid) in (''0'',''-9'','''')
					or trim(segment) in (''0'',''-9'','''')
					or trim(fips) in (''0'',''-9'','''')
					or trim(effective_date) in (''0'',''-9'','''')
					;';



		END IF;
		
	
	END LOOP;

END;
$BODY$
  LANGUAGE 'plpgsql' VOLATILE;
ALTER FUNCTION public.clean_project_future_year_inventory_control_programs(int) OWNER TO emf;

