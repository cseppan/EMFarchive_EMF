CREATE OR REPLACE FUNCTION run_max(double precision, text, text)
  RETURNS numeric AS
$BODY$
	if {![info exists GD(max.$2.$3)]} {
		set GD(max.$2.$3) 0.00
	}
	if {[argisnull 1]} {
		return $GD(max.$2.$3)
	} else {
		if {$GD(max.$2.$3) > $1} {
			return $GD(max.$2.$3)
		} else {
			set GD(max.$2.$3) [expr $1]
			return [set GD(max.$2.$3) [expr $1]]
		}
	}
$BODY$
  LANGUAGE 'pltcl' IMMUTABLE;
ALTER FUNCTION run_max(numeric, text, text) OWNER TO emf;

CREATE OR REPLACE FUNCTION public.running_max_previous_value(double precision, text, text)
  RETURNS double precision AS
$BODY$
	
	if {![info exists GD(max.$2.$3)]} {
		set GD(max.$2.$3) 0.00
	}
	if {[argisnull 1]} {
		return $GD(max.$2.$3)
	} else {
		if {$GD(max.$2.$3) > $1} {
			return $GD(max.$2.$3)
		} else {
			set max_prev_value [expr $GD(max.$2.$3)]
			set GD(max.$2.$3) [expr $1]
			return $max_prev_value
		}
	}


$BODY$
  LANGUAGE 'pltcl' IMMUTABLE;
ALTER FUNCTION public.running_max_previous_value(double precision, text, text) OWNER TO emf;

			return [set GD(max.$2.$3) [expr $1]]


			spi_exec "SELECT 'GD(max.$2.$3)' AS max_prev_value"
     if {![ info exists GD(plan) ]} {
         # prepare the saved plan on the first call
         set GD(plan) [ spi_prepare \\
                 "SELECT count(*) AS cnt FROM t1 WHERE num >= \\$1 AND num <= \\$2" \\
                 double precision ]
     }
     spi_execp -count 1 $GD(plan) [ list $1 $2 ]
     return $cnt	

CREATE OR REPLACE FUNCTION run_max(double precision, text)
  RETURNS numeric AS
'select run_max($1,$2,statement_timestamp()::text)'
  LANGUAGE 'sql' IMMUTABLE STRICT;
ALTER FUNCTION run_max(numeric, text) OWNER TO emf;

CREATE OR REPLACE FUNCTION public.running_max_previous_value(double precision, text)
  RETURNS double precision AS
'select running_max_previous_value($1,$2,statement_timestamp()::text)'
  LANGUAGE 'sql' IMMUTABLE STRICT;
ALTER FUNCTION public.running_max_previous_value(double precision, text) OWNER TO emf;

CREATE OR REPLACE FUNCTION run_max_post(double precision, text) RETUNS AS double precision $$
DECLARE
	prev_max_value varchar(64) := '';
BEGIN
	prev_max_value = run_max($1,$2,statement_timestamp()::text);
	RETURN;
END;
$$ LANGUAGE plpgsql;
