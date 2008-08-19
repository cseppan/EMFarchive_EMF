package gov.epa.emissions.framework.services.cost.analysis.projectFutureYearInventory;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyTask;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.SQLException;
import java.util.Date;

public class StrategyTask extends AbstractStrategyTask {

    private StrategyLoader loader;
    
    public StrategyTask(ControlStrategy controlStrategy, User user, 
            DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) throws EmfException {
        super(controlStrategy, user, 
                dbServerFactory, sessionFactory);
        this.loader = new StrategyLoader(user, dbServerFactory, 
                sessionFactory, controlStrategy);
    }

    public void run() throws EmfException {
//        super.run(loader);
        
        //get rid of strategy results
        deleteStrategyResults();

        //run any pre processes
        try {
            beforeRun();
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            //
        }

        String status = "";
        try {
            //process/load each input dataset
            ControlStrategyInputDataset[] controlStrategyInputDatasets = controlStrategy.getControlStrategyInputDatasets();
            for (int i = 0; i < controlStrategyInputDatasets.length; i++) {
                ControlStrategyResult result = null;
                try {
                    result = loader.loadStrategyResult(controlStrategyInputDatasets[i]);
                    recordCount = loader.getRecordCount();
                    result.setRecordCount(recordCount);
                    status = "Completed.";
                } catch (Exception e) {
                    e.printStackTrace();
                    status = "Failed. Error processing input dataset: " + controlStrategyInputDatasets[i].getInputDataset().getName() + ". " + e.getMessage();
                    setStatus(status);
                } finally {
                    if (result != null) {
                        result.setCompletionTime(new Date());
                        result.setRunStatus(status);
                        saveControlStrategyResult(result);
                        strategyResultList.add(result);
                        addStatus(controlStrategyInputDatasets[i]);
                    }
                    //make sure somebody hasn't cancelled this run.
                    if (isRunStatusCancelled()) {
//                        status = "Cancelled. Strategy run was cancelled: " + controlStrategy.getName();
//                        setStatus(status);
                        throw new EmfException("Strategy run was cancelled.");
                    }
                }
            }
//            deleteStrategyResults();
        } catch (Exception e) {
            status = "Failed. Error processing input dataset";
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            //run any post processes
            try {
                afterRun();
            } catch (Exception e) {
                status = "Failed. Error processing input dataset";
                e.printStackTrace();
                throw new EmfException(e.getMessage());
            } finally {
                loader.disconnectDbServer();
                disconnectDbServer();
            }
        }
        //if (loader.getMessageDatasetRecordCount() == 0) deleteStrategyMessageResult();
    }

    public void afterRun() {
        //
    }

    public void beforeRun() throws EmfException {
        try {
            //next lets replace missing data and index the control program packet tables...
            for (ControlProgram controlProgram : controlStrategy.getControlPrograms()) {
                    convertMissingColumnValues(controlProgram);
                    indexTable(controlProgram);
            }
            //next lets validate the control program dataset tables, make sure format is correct, no data is missing, etc...
            validateControlPrograms();
        } catch (EmfException e) {
            throw e;
        }
    }

    private void validateControlPrograms() throws EmfException {
        try {
            datasource.query().execute("select public.validate_project_future_year_inventory_control_programs(" + controlStrategy.getId() + "::integer);");
        } catch (SQLException e) {
            throw new EmfException(e.getMessage());
        } finally {
            //
        }
    }

    private void convertMissingColumnValues(ControlProgram controlProgram) {
        String query = "";
        EmfDataset dataset = controlProgram.getDataset();
        if (controlProgram.getControlProgramType().getName().equals(ControlProgramType.plantClosure)) {
            query = "update " + qualifiedEmissionTableName(dataset)
            + "         set plantid = case when plantid is null or trim(plantid) = '0' or trim(plantid) = '-9' or trim(plantid) = '' then null::character varying(15) else plantid end,"
            + "         pointid = case when pointid is null or trim(pointid) = '0' or trim(pointid) = '-9' or trim(pointid) = '' then null::character varying(15) else pointid end,"
            + "         stackid = case when stackid is null or trim(stackid) = '0' or trim(stackid) = '-9' or trim(stackid) = '' then null::character varying(15) else stackid end,"
            + "         segment = case when segment is null or trim(segment) = '0' or trim(segment) = '-9' or trim(segment) = '' then null::character varying(15) else segment end,"
            + "         fips = case when fips is null or trim(fips) = '0' or trim(fips) = '-9' or trim(fips) = '' then null::character varying(6) else fips end "
            + "where trim(plantid) in ('0','-9','')"
            + "         or trim(pointid) in ('0','-9','')"
            + "         or trim(stackid) in ('0','-9','')"
            + "         or trim(segment) in ('0','-9','')"
            + "         or trim(fips) in ('0','-9','');"
            + "vacuum analyze "  + qualifiedEmissionTableName(dataset) + ";";
        } else if (controlProgram.getControlProgramType().getName().equals(ControlProgramType.projection)) {
            if (dataset.getDatasetType().getName().equals(DatasetType.projectionPacket)) {
                query = "update " + qualifiedEmissionTableName(dataset)
                + "         set plantid = case when plantid is null or trim(plantid) = '0' or trim(plantid) = '-9' or trim(plantid) = '' then null::character varying(15) else plantid end,"
                + "         pointid = case when pointid is null or trim(pointid) = '0' or trim(pointid) = '-9' or trim(pointid) = '' then null::character varying(15) else pointid end,"
                + "         stackid = case when stackid is null or trim(stackid) = '0' or trim(stackid) = '-9' or trim(stackid) = '' then null::character varying(15) else stackid end,"
                + "         segment = case when segment is null or trim(segment) = '0' or trim(segment) = '-9' or trim(segment) = '' then null::character varying(15) else segment end,"
                + "         fips = case when fips is null or trim(fips) = '0' or trim(fips) = '-9' or trim(fips) = '' then null::character varying(6) else fips end,"
                + "         scc = case when scc is null or trim(scc) = '0' or trim(scc) = '-9' or trim(scc) = '' then null::character varying(10) else scc end,"
                + "         mact = case when mact is null or trim(mact) = '0' or trim(mact) = '-9' or trim(mact) = '' then null::character varying(6) else mact end,"
                + "         sic = case when sic is null or trim(sic) = '0' or trim(sic) = '-9' or trim(sic) = '' then null::character varying(4) else sic end "
                + "where trim(plantid) in ('0','-9','')"
                + "         or trim(pointid) in ('0','-9','')"
                + "         or trim(stackid) in ('0','-9','')"
                + "         or trim(segment) in ('0','-9','')"
                + "         or trim(fips) in ('0','-9','')"
                + "         or trim(scc) in ('0','-9','')"
                + "         or trim(mact) in ('0','-9','')"
                + "         or trim(sic) in ('0','-9','');"
                + "vacuum analyze "  + qualifiedEmissionTableName(dataset) + ";";
            }
        } else if (controlProgram.getControlProgramType().getName().equals(ControlProgramType.control)) {
            if (dataset.getDatasetType().getName().equals(DatasetType.controlPacket)) {
                query = "update " + qualifiedEmissionTableName(dataset)
                + "         set plantid = case when plantid is null or trim(plantid) = '0' or trim(plantid) = '-9' or trim(plantid) = '' then null::character varying(15) else plantid end,"
                + "         pointid = case when pointid is null or trim(pointid) = '0' or trim(pointid) = '-9' or trim(pointid) = '' then null::character varying(15) else pointid end,"
                + "         stackid = case when stackid is null or trim(stackid) = '0' or trim(stackid) = '-9' or trim(stackid) = '' then null::character varying(15) else stackid end,"
                + "         segment = case when segment is null or trim(segment) = '0' or trim(segment) = '-9' or trim(segment) = '' then null::character varying(15) else segment end,"
                + "         fips = case when fips is null or trim(fips) = '0' or trim(fips) = '-9' or trim(fips) = '' then null::character varying(6) else fips end,"
                + "         scc = case when scc is null or trim(scc) = '0' or trim(scc) = '-9' or trim(scc) = '' then null::character varying(10) else scc end,"
                + "         mact = case when mact is null or trim(mact) = '0' or trim(mact) = '-9' or trim(mact) = '' then null::character varying(6) else mact end,"
                + "         sic = case when sic is null or trim(sic) = '0' or trim(sic) = '-9' or trim(sic) = '' then null::character varying(4) else sic end,"
                + "         pri_cm_abbrev = case when pri_cm_abbrev is null or trim(pri_cm_abbrev) = '0' or trim(pri_cm_abbrev) = '-9' or trim(pri_cm_abbrev) = '' then null::character varying(4) else pri_cm_abbrev end "
                + "where trim(plantid) in ('0','-9','')"
                + "         or trim(pointid) in ('0','-9','')"
                + "         or trim(stackid) in ('0','-9','')"
                + "         or trim(segment) in ('0','-9','')"
                + "         or trim(fips) in ('0','-9','')"
                + "         or trim(scc) in ('0','-9','')"
                + "         or trim(mact) in ('0','-9','')"
                + "         or trim(sic) in ('0','-9','')"
                + "         or trim(pri_cm_abbrev) in ('0','-9','');"
                + "vacuum analyze "  + qualifiedEmissionTableName(dataset) + ";";
            }
        }
        
//        System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            //throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //
        }
    }

    private void indexTable(ControlProgram controlProgram) {
        String query = "";
        EmfDataset dataset = controlProgram.getDataset();
        String tableName = emissionTableName(dataset);
        String qualifiedTableName = qualifiedEmissionTableName(dataset);
        if (controlProgram.getControlProgramType().getName().equals(ControlProgramType.plantClosure)) {
            query = "CREATE INDEX " + (("comp_" + tableName).length() >= 63 - 5 ? "comp_" + tableName.substring(6, (tableName.length() > 63 ? 63 : tableName.length())) : "comp_" + tableName) + " "
                + "ON "  + qualifiedTableName + " "
                + "USING btree "
                + "(fips, plantid, pointid, stackid, segment); "
                + "CREATE INDEX " + (("fips_" + tableName).length() >= 63 - 5 ? "fips_" + tableName.substring(6, (tableName.length() > 63 ? 63 : tableName.length())) : "fips_" + tableName) + " "
            + "ON "  + qualifiedTableName + " "
            + "USING btree "
            + "(fips); "
            + "CREATE INDEX " + (("plantid_" + tableName).length() >= 63 - 8 ? "plantid_" + tableName.substring(9, (tableName.length() > 63 ? 63 : tableName.length())) : "plantid_" + tableName) + " "
            + "ON "  + qualifiedTableName + " "
            + "USING btree "
            + "(plantid); "
            + "CREATE INDEX " + (("pointid_" + tableName).length() >= 63 - 8 ? "pointid_" + tableName.substring(9, (tableName.length() > 63 ? 63 : tableName.length())) : "pointid_" + tableName) + " "
            + "ON "  + qualifiedTableName + " "
            + "USING btree "
            + "(pointid); "
            + "CREATE INDEX " + (("segment_" + tableName).length() >= 63 - 8 ? "segment_" + tableName.substring(9, (tableName.length() > 63 ? 63 : tableName.length())) : "segment_" + tableName) + " "
            + "ON "  + qualifiedTableName + " "
            + "USING btree "
            + "(segment); "
            + "CREATE INDEX " + (("stackid_" + tableName).length() >= 63 - 8 ? "stackid_" + tableName.substring(9, (tableName.length() > 63 ? 63 : tableName.length())) : "stackid_" + tableName) + " "
            + "ON "  + qualifiedTableName + " "
            + "USING btree "
            + "(stackid);"
            + "analyze "  + qualifiedTableName + ";";
        } else if (controlProgram.getControlProgramType().getName().equals(ControlProgramType.projection)) {
            if (dataset.getDatasetType().getName().equals(DatasetType.projectionPacket)) {
                query = "CREATE INDEX " + (("comp_" + tableName).length() >= 63 - 5 ? "comp_" + tableName.substring(6, (tableName.length() > 63 ? 63 : tableName.length())) : "comp_" + tableName) + " "
                + "ON "  + qualifiedTableName + " "
                + "USING btree "
                + "(fips, plantid, pointid, stackid, segment, scc, poll, sic, mact); "
                + "CREATE INDEX " + (("fips_" + tableName).length() >= 63 - 5 ? "fips_" + tableName.substring(6, (tableName.length() > 63 ? 63 : tableName.length())) : "fips_" + tableName) + " "
                + "ON "  + qualifiedTableName + " "
                + "USING btree "
                + "(fips); "
                + "CREATE INDEX " + (("mact_" + tableName).length() >= 63 - 5 ? "mact_" + tableName.substring(6, (tableName.length() > 63 ? 63 : tableName.length())) : "mact_" + tableName) + " "
                + "ON "  + qualifiedTableName + " "
                + "USING btree "
                + "(mact); "
                + "CREATE INDEX " + (("plantid_" + tableName).length() >= 63 - 8 ? "plantid_" + tableName.substring(9, (tableName.length() > 63 ? 63 : tableName.length())) : "plantid_" + tableName) + " "
                + "ON "  + qualifiedTableName + " "
                + "USING btree "
                + "(plantid); "
                + "CREATE INDEX " + (("pointid_" + tableName).length() >= 63 - 8 ? "pointid_" + tableName.substring(9, (tableName.length() > 63 ? 63 : tableName.length())) : "pointid_" + tableName) + " "
                + "ON "  + qualifiedTableName + " "
                + "USING btree "
                + "(pointid); "
                + "CREATE INDEX " + (("poll_" + tableName).length() >= 63 - 5 ? "poll_" + tableName.substring(6, (tableName.length() > 63 ? 63 : tableName.length())) : "poll_" + tableName) + " "
                + "ON "  + qualifiedTableName + " "
                + "USING btree "
                + "(poll); "
                + "CREATE INDEX " + (("scc_" + tableName).length() >= 63 - 4 ? "scc_" + tableName.substring(5, (tableName.length() > 63 ? 63 : tableName.length())) : "scc_" + tableName) + " "
                + "ON "  + qualifiedTableName + " "
                + "USING btree "
                + "(scc); "
                + "CREATE INDEX " + (("segment_" + tableName).length() >= 63 - 8 ? "segment_" + tableName.substring(9, (tableName.length() > 63 ? 63 : tableName.length())) : "segment_" + tableName) + " "
                + "ON "  + qualifiedTableName + " "
                + "USING btree "
                + "(segment); "
                + "CREATE INDEX " + (("sic_" + tableName).length() >= 63 - 4 ? "sic_" + tableName.substring(5, (tableName.length() > 63 ? 63 : tableName.length())) : "sic_" + tableName) + " "
                + "ON "  + qualifiedTableName + " "
                + "USING btree "
                + "(sic); "
                + "CREATE INDEX " + (("stackid_" + tableName).length() >= 63 - 8 ? "stackid_" + tableName.substring(9, (tableName.length() > 63 ? 63 : tableName.length())) : "stackid_" + tableName) + " "
                + "ON "  + qualifiedTableName + " "
                + "USING btree "
                + "(stackid);"
                + "analyze "  + qualifiedTableName + ";";
           }
        } else if (controlProgram.getControlProgramType().getName().equals(ControlProgramType.control)) {
            if (dataset.getDatasetType().getName().equals(DatasetType.controlPacket)) {
                query = "CREATE INDEX " + (("comp_" + tableName).length() >= 63 - 5 ? "comp_" + tableName.substring(6, (tableName.length() > 63 ? 63 : tableName.length())) : "comp_" + tableName) + " "
                    + "ON "  + qualifiedTableName + " "
                    + "USING btree "
                    + "(fips, plantid, pointid, stackid, segment, scc, poll, sic, mact); "
                    + "CREATE INDEX " + (("fips_" + tableName).length() >= 63 - 5 ? "fips_" + tableName.substring(6, (tableName.length() > 63 ? 63 : tableName.length())) : "fips_" + tableName) + " "
                    + "ON "  + qualifiedTableName + " "
                    + "USING btree "
                    + "(fips); "
                    + "CREATE INDEX " + (("mact_" + tableName).length() >= 63 - 5 ? "mact_" + tableName.substring(6, (tableName.length() > 63 ? 63 : tableName.length())) : "mact_" + tableName) + " "
                    + "ON "  + qualifiedTableName + " "
                    + "USING btree "
                    + "(mact); "
                    + "CREATE INDEX " + (("plantid_" + tableName).length() >= 63 - 8 ? "plantid_" + tableName.substring(9, (tableName.length() > 63 ? 63 : tableName.length())) : "plantid_" + tableName) + " "
                    + "ON "  + qualifiedTableName + " "
                    + "USING btree "
                    + "(plantid); "
                    + "CREATE INDEX " + (("pointid_" + tableName).length() >= 63 - 8 ? "pointid_" + tableName.substring(9, (tableName.length() > 63 ? 63 : tableName.length())) : "pointid_" + tableName) + " "
                    + "ON "  + qualifiedTableName + " "
                    + "USING btree "
                    + "(pointid); "
                    + "CREATE INDEX " + (("poll_" + tableName).length() >= 63 - 5 ? "poll_" + tableName.substring(6, (tableName.length() > 63 ? 63 : tableName.length())) : "poll_" + tableName) + " "
                    + "ON "  + qualifiedTableName + " "
                    + "USING btree "
                    + "(poll); "
                    + "CREATE INDEX " + (("scc_" + tableName).length() >= 63 - 4 ? "scc_" + tableName.substring(5, (tableName.length() > 63 ? 63 : tableName.length())) : "scc_" + tableName) + " "
                    + "ON "  + qualifiedTableName + " "
                    + "USING btree "
                    + "(scc); "
                    + "CREATE INDEX " + (("segment_" + tableName).length() >= 63 - 8 ? "segment_" + tableName.substring(9, (tableName.length() > 63 ? 63 : tableName.length())) : "segment_" + tableName) + " "
                    + "ON "  + qualifiedTableName + " "
                    + "USING btree "
                    + "(segment); "
                    + "CREATE INDEX " + (("sic_" + tableName).length() >= 63 - 4 ? "sic_" + tableName.substring(5, (tableName.length() > 63 ? 63 : tableName.length())) : "sic_" + tableName) + " "
                    + "ON "  + qualifiedTableName + " "
                    + "USING btree "
                    + "(sic); "
                    + "CREATE INDEX " + (("stackid_" + tableName).length() >= 63 - 8 ? "stackid_" + tableName.substring(9, (tableName.length() > 63 ? 63 : tableName.length())) : "stackid_" + tableName) + " "
                    + "ON "  + qualifiedTableName + " "
                    + "USING btree "
                    + "(stackid);"
                    + "analyze "  + qualifiedTableName + ";";
            }
        }
        
//        System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            //throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //
        }
    }
}