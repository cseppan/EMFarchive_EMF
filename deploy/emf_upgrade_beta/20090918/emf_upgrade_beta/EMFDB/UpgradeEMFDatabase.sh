#!/bin/sh

## Upgrades EMF cost schema 
##
## Should be run from the same directory as this file 
##
##------------------------------------------------------------##

## set environtment variables for script
export POSTGRESBINDIR=/usr/bin
export EMFDATABASE=EMF

$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf -c "update emf.properties  set \"value\"='v2.4 - 09/16/2009' where name='EMF-version';"
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/upgrade_cost_schema.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/populate_max_emis_red_strategy_messages.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/populate_least_cost_strategy_worksheet.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/populate_least_cost_strategy_detailed_result.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/cost_equations.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/run_max_emis_red_strategy.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < cost/run_project_future_year_inventory.sql
$POSTGRESBINDIR/psql -d $EMFDATABASE -U emf < data/upgrade_data_schema.sql
