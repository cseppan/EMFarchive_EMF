drop FUNCTION public.eliminate_least_cost_strategy_source_measures(integer, 
	integer, 
	integer);
	
CREATE OR REPLACE FUNCTION public.eliminate_least_cost_strategy_source_measures(int_control_strategy_id integer, 
	input_dataset_id integer, 
	input_dataset_version integer) RETURNS void AS $$
DECLARE
	inv_table_name varchar(64) := '';
	worksheet_dataset_id integer := null;
	worksheet_table_name varchar(64) := '';
	target_pollutant varchar;
	gimme_count integer := 0;
	include_unspecified_costs boolean := true; 
BEGIN
--	SET work_mem TO '256MB';
--	SET enable_seqscan TO 'off';
--	SET enable_nestloop TO 'on';

	-- get the input dataset info
	select lower(i.table_name)
	from emf.internal_sources i
	where i.dataset_id = input_dataset_id
	into inv_table_name;

	select sr.detailed_result_dataset_id,
		lower(i.table_name)
	from emf.strategy_results sr
		inner join emf.internal_sources i
		on i.dataset_id = sr.detailed_result_dataset_id
		inner join emf.strategy_result_types srt
		on srt.id = sr.strategy_result_type_id
	where sr.control_strategy_id = int_control_strategy_id 
		and srt.name = 'Least Cost Control Measure Worksheet'
	-- get the most recent worksheet
	order by sr.id desc
	into worksheet_dataset_id,
		worksheet_table_name;

	-- get target pollutant
	SELECT p.name,
		coalesce(cs.include_unspecified_costs,true)
	FROM emf.control_strategies cs
		inner join emf.pollutants p
		on p.id = cs.pollutant_id
	where cs.id = int_control_strategy_id
	INTO target_pollutant,
		include_unspecified_costs;
		
--	raise notice '%', 'start ' || clock_timestamp();


	-- look for ties too, since we have a double precision data type. thats why a pct diff is used instead of
	-- tbl.emis_reduction <= public.running_max_previous_value(tbl.emis_reduction, ''s'' || tbl.source)
	execute 'update emissions.' || worksheet_table_name || '
	set status = 0
	where record_id in (
		select case when 
		coalesce(tbl.emis_reduction, 0.0) <> 0.0 and (tbl.emis_reduction - public.running_max_previous_value(tbl.emis_reduction, ''s'' || tbl.source)) / tbl.emis_reduction <= 0.001
--		tbl.emis_reduction <= public.running_max_previous_value(tbl.emis_reduction, ''s'' || tbl.source) 
		then tbl.record_id else null::integer 
		end
		from (
		select source, record_id, emis_reduction, marginal, source_poll_cnt
		from emissions.' || worksheet_table_name || '
		where poll = ' || quote_literal(target_pollutant) || '
		order by source, marginal, emis_reduction desc, source_poll_cnt desc, record_id		
		) tbl
		order by tbl.source, tbl.marginal, tbl.emis_reduction desc, tbl.source_poll_cnt desc, tbl.record_id
	)';

	if not include_unspecified_costs then 
		-- look for measure cost freebies, get rid of them
		execute 'update emissions.' || worksheet_table_name || '
		set status = 0
		where record_id in (
			select tbl2.record_id
			from (
			select CM_Id, source--, record_id
			from emissions.' || worksheet_table_name || '
			group by CM_Id, source
			having sum(annual_cost) = 0.0
			) tbl
			inner join emissions.' || worksheet_table_name || ' tbl2
			on tbl2.CM_Id = tbl.CM_Id
			and tbl2.source = tbl.source
		)';

	end if;


	get diagnostics gimme_count = row_count;
--	raise notice '%', 'get rid of measures - you''d never pay more to get less (or the same) - emissions.' || worksheet_table_name || ' count = ' || gimme_count || ' - ' || clock_timestamp();

	-- remove unsuitable measures (get rid of target and cobenefit pollutants)
	execute 'update emissions.' || worksheet_table_name || ' as detailed_result
			set status = 0
		where record_id in (
			select ap.record_id
			from emissions.' || worksheet_table_name || ' tp
				inner join emissions.' || worksheet_table_name || ' ap
				on 
				ap.source = tp.source
				and ap.cm_id = tp.cm_id
			where tp.status = 0
				and tp.poll = ' || quote_literal(target_pollutant) || ');';
				
--	raise notice '%', 'delete from emissions.' || worksheet_table_name || ' where status = 0 ' || ' - ' || clock_timestamp();

	return;
END;
$$ LANGUAGE plpgsql;

