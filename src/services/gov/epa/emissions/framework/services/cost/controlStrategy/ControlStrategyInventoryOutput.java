package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.VersionedDatasetQuery;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.QAStepTask;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Session;

public class ControlStrategyInventoryOutput {

    private ControlStrategy controlStrategy;

    private DatasetCreator creator;

    private TableFormat tableFormat;

    private StatusDAO statusServices;

    private User user;

    private HibernateSessionFactory sessionFactory;

    private DbServer dbServer;

    public ControlStrategyInventoryOutput(User user, ControlStrategy controlStrategy,
            HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) throws Exception {
        this.controlStrategy = controlStrategy;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.dbServer = dbServerFactory.getDbServer();
        creator = new DatasetCreator("ControlledInventory_", "CSINVEN_", controlStrategy, user, sessionFactory, dbServerFactory);
        this.tableFormat = new FileFormatFactory(dbServer).tableFormat(datasetType(controlStrategy));
        this.statusServices = new StatusDAO(sessionFactory);
    }

    private DatasetType datasetType(ControlStrategy strategy) throws EmfException {
        DatasetType type = strategy.getDatasetType();
        if (type == null)
            throw new EmfException("Please select a dataset type");
        return type;
    }

    public void create() throws Exception {
        Datasource datasource = dbServer.getEmissionsDatasource();
        TableCreator tableCreator = new TableCreator(datasource);
        EmfDataset inputDataset = controlStrategy.getInputDatasets()[0];
        String inputTable = inputTable(inputDataset);
        doCreateInventory(dbServer, datasource, tableCreator, inputDataset, inputTable);
        dbServer.disconnect();
    }

    private void doCreateInventory(DbServer server, Datasource datasource, TableCreator tableCreator,
            EmfDataset inputDataset, String inputTable) throws EmfException, Exception, SQLException {
        String outputInventoryTableName = creator.outputTableName();
        createTable(outputInventoryTableName, tableCreator);
        startStatus(statusServices);
        try {
            copyAndUpdateData(server, datasource, inputDataset, inputTable, outputInventoryTableName);
        } catch (Exception e) {
            failStatus(statusServices, e.getMessage());
            tableCreator.drop(outputInventoryTableName);
            e.printStackTrace();
            throw e;
        } finally {
            setandRunQASteps();
        }
        endStatus(statusServices);
    }

//    private void copyAndUpdateData(EmfDbServer server, Datasource datasource, EmfDataset inputDataset,
    private void copyAndUpdateData(DbServer server, Datasource datasource, EmfDataset inputDataset,
            String inputTable, String outputInventoryTableName) throws EmfException {
        copyDataFromOriginalTable(inputTable, outputInventoryTableName, version(inputDataset, controlStrategy
                .getDatasetVersion()), inputDataset, datasource);

        ControlStrategyResult result = controlStrategyResults(controlStrategy);
        updateDataWithDetailDatasetTable(outputInventoryTableName, detailDatasetTable(result), server
                .getEmissionsDatasource());

        EmfDataset dataset = creator.addDataset(inputDataset, controlStrategy.getDatasetType(), description(inputDataset),
                tableFormat, inputDataset.getName(), datasource);
        updateDatasetIdAndVersion(outputInventoryTableName, server.getEmissionsDatasource(), dataset.getId());

        updateControlStrategyResults(result, dataset);
    }

    private void setandRunQASteps() throws EmfException {
        try {
            ControlStrategyResult result = controlStrategyResults(controlStrategy);
            EmfDataset controlledDataset = (EmfDataset) result.getControlledInventoryDataset();
            QAStepTask qaTask = new QAStepTask(controlledDataset, controlledDataset.getDefaultVersion(), user,
                    sessionFactory, dbServer);
            qaTask.runSummaryQASteps(qaTask.getDefaultSummaryQANames());
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        }
    }

    private String description(EmfDataset inputDataset) {
        String startingDesc = inputDataset.getDescription();
        if ((startingDesc.indexOf("FIPS,SCC") > 0) || (startingDesc.indexOf("\"FIPS\",") > 0))
        {
           return startingDesc;
        }
        return inputDataset.getDescription() + "#" + "Implements control strategy: " + controlStrategy.getName() + "\n" +
          "#DESC FIPS,SCC,SIC,MACT,SRCTYPE,POLL,ANN_EMIS,AVD_EMIS,CEFF,REFF,RPEN,PRI_DEV,SEC_DEV,DATA_SOURCE,YEAR,TRIBAL_CODE,"+
          "MACT_FLAG,COMPLIANCE_STATUS,START_DATE,END_DATE,WINTER_PCT,SPRING_PCT,SUMMER_PCT,FALL_PCT,DAYS_PER_WEEK,WEEKS_PER_YEAR,HOURS_PER_DAY,"+
          "HOURS_PER_YEAR,PERIOD_DAYS_PER_WEEK,PERIOD_WEEKS_PER_YEAR,PERIOD_HOURS_OF_DAY,PERIOD_HOURS_PER_PERIOD\n";
          // TODO: need to make it so that exporters automatically output the column descs instead of putting it here
    }

    private ControlStrategyResult controlStrategyResults(ControlStrategy controlStrategy) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategyDAO dao = new ControlStrategyDAO();
            ControlStrategyResult result = dao.controlStrategyResult(controlStrategy, session);
            if (result == null)
                throw new EmfException("You have to run the control strategy to create control inventory output");
            return result;
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }

    }

    private void updateControlStrategyResults(ControlStrategyResult result, EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategyDAO dao = new ControlStrategyDAO();
            result.setControlledInventoryDataset(dataset);
            dao.updateControlStrategyResults(result, session);
        } catch (Exception e) {
            throw new EmfException("Could not update control strategy results: " + e.getMessage());
        } finally {
            session.close();
        }

    }

    private Version version(EmfDataset inputDataset, int datasetVersion) {
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(inputDataset.getId(), datasetVersion, session);
        } finally {
            session.close();
        }
    }

    private String inputTable(EmfDataset inputDataset) throws EmfException {
        InternalSource[] sources = inputDataset.getInternalSources();
        if (sources.length > 1) {
            throw new EmfException(
                    "At this moment datasets with multiple tables are not supported for creating a inventory output");
        }
        String inputTable = sources[0].getTable();
        return inputTable;
    }

    private void updateDatasetIdAndVersion(String tableName, Datasource datasource, int id) throws EmfException {
        String query = "UPDATE " + qualifiedTable(tableName, datasource) + " SET dataset_id=" + id
                + ", version=0, delete_versions=DEFAULT";

        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Could not update dataset_id column in the new inventory table '" + tableName + "'");
        }

    }

    private void updateDataWithDetailDatasetTable(String outputTable, String detailResultTable,
            Datasource emissionsDatasource) throws EmfException {
        String query = updateQuery(outputTable, detailResultTable, emissionsDatasource);
        try {
            emissionsDatasource.query().execute(query);
        } catch (SQLException e) {
            System.err.println(query);
            e.printStackTrace();
            throw new EmfException("Could not update inventory table '" + outputTable + "' using detail result table '"
                    + detailResultTable + "'");
        }

    }

    private String updateQuery(String outputTable, String detailResultTable, Datasource datasource) {
        boolean pointDatasetType = controlStrategy.getDatasetType().getName().equalsIgnoreCase("ORL Point Inventory (PTINV)");
        String sql = "update " + qualifiedTable(outputTable, datasource) + " as o "
        + "set ceff = case when ann_emis <> 0 then (1 - b.final_emissions / ann_emis) * 100 else 0 end, "
        + "ann_emis = b.final_emissions, "
        + "reff = 100 "
        + (!pointDatasetType ? ", rpen = 100 " : " ")
        + "FROM ( "
        + "SELECT source_id, max(final_emissions) as final_emissions "
        + "FROM " + qualifiedTable(detailResultTable, datasource) + " "
        + "group by source_id "
        + ") as b "
        + "WHERE o.record_id = b.source_id";
        return sql;
    }

    private String detailDatasetTable(ControlStrategyResult result) {
        Dataset detailedResultDataset = result.getDetailedResultDataset();
        return detailedResultDataset.getInternalSources()[0].getTable();
    }

    private void copyDataFromOriginalTable(String inputTable, String outputInventoryTableName, Version version,
            Dataset dataset, Datasource emissionsDatasource) throws EmfException {
        String query = copyDataFromOriginalTableQuery(inputTable, outputInventoryTableName, version, dataset,
                emissionsDatasource);
        try {
            emissionsDatasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Error occured when copying data from " + inputTable + " to "
                    + outputInventoryTableName + "\n" + e.getMessage());
        }
    }

    private String copyDataFromOriginalTableQuery(String inputTable, String outputTable, Version version,
            Dataset dataset, Datasource datasource) {
        String versionedQuery = new VersionedDatasetQuery(version, dataset).generate(qualifiedTable(inputTable,
                datasource));

        return "INSERT INTO " + qualifiedTable(outputTable, datasource) + " " + versionedQuery;
    }

    private String qualifiedTable(String table, Datasource datasource) {
        return datasource.getName() + "." + table;
    }

    private void createTable(String outputInventoryTableName, TableCreator creator) throws EmfException {
        try {
            creator.create(outputInventoryTableName, tableFormat);
        } catch (Exception e) {
            throw new EmfException("Could not create table: " + outputInventoryTableName + "\n" + e.getMessage());
        }
    }

    private void endStatus(StatusDAO statusServices) {
        String end = "Finished creating a controlled inventory for strategy "+controlStrategy.getName();
        Status status = status(user, end);
        statusServices.add(status);
    }

    private void failStatus(StatusDAO statusServices, String message) {
        String end = "Failed to create a controlled inventory for strategy "+controlStrategy.getName()+
           ": " + message;
        Status status = status(user, end);
        statusServices.add(status);
    }

    private void startStatus(StatusDAO statusServices) {
        String start = "Started creating controlled inventory of type '" + controlStrategy.getDatasetType()
                + "' using control strategy '" + controlStrategy.getName();
        Status status = status(user, start);
        statusServices.add(status);
    }

    private Status status(User user, String message) {
        Status status = new Status();
        status.setUsername(user.getUsername());
        status.setType("Controlled Inventory");
        status.setMessage(message);
        status.setTimestamp(new Date());
        return status;
    }

}
