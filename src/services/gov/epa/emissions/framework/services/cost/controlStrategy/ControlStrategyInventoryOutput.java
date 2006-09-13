package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.VersionedDatasetQuery;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
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

    public ControlStrategyInventoryOutput(User user, ControlStrategy controlStrategy,
            HibernateSessionFactory sessionFactory) throws Exception {
        this.controlStrategy = controlStrategy;
        this.user = user;
        creator = new DatasetCreator("CSINVEN_", controlStrategy, user, sessionFactory);
        this.tableFormat = new FileFormatFactory().tableFormat(datasetType(controlStrategy));
        this.statusServices = new StatusDAO();
    }

    private DatasetType datasetType(ControlStrategy strategy) throws EmfException {
        DatasetType type = strategy.getDatasetType();
        if (type == null)
            throw new EmfException("Please select a dataset type");
        return type;
    }

    public void create() throws Exception {
        EmfDbServer server = new EmfDbServer();
        Datasource datasource = server.getEmissionsDatasource();
        TableCreator tableCreator = new TableCreator(datasource);
        EmfDataset inputDataset = controlStrategy.getInputDatasets()[0];
        String inputTable = inputTable(inputDataset);
        doCreateInventory(server, datasource, tableCreator, inputDataset, inputTable);
    }

    private void doCreateInventory(EmfDbServer server, Datasource datasource, TableCreator tableCreator,
            EmfDataset inputDataset, String inputTable) throws EmfException, Exception, SQLException {
        String outputInventoryTableName = creator.outputTableName();
        createTable(outputInventoryTableName, tableCreator);
        startStatus(statusServices);
        try {
            copyAndUpdateData(server, datasource, inputDataset, inputTable, outputInventoryTableName);
        } catch (Exception e) {
            failStatus(statusServices, e.getMessage());
            tableCreator.drop(outputInventoryTableName);
            throw e;
        } finally {
            server.disconnect();
        }
        endStatus(statusServices);
    }

    private void copyAndUpdateData(EmfDbServer server, Datasource datasource, EmfDataset inputDataset,
            String inputTable, String outputInventoryTableName) throws EmfException {
        copyDataFromOriginalTable(inputTable, outputInventoryTableName, version(inputDataset, controlStrategy
                .getDatasetVersion()), inputDataset, datasource);

        updateDataWithDetailDatasetTable(outputInventoryTableName, detailDatasetTable(controlStrategy), server
                .getEmissionsDatasource());

        EmfDataset dataset = creator.addDataset(controlStrategy.getDatasetType(), description(inputDataset),
                tableFormat, inputDataset.getName(), datasource);
        updateDatasetIdAndVersion(outputInventoryTableName, server.getEmissionsDatasource(), dataset.getId());

        updateControlStrategyResults(controlStrategy, dataset);
    }

    private String description(EmfDataset inputDataset) {
        return inputDataset.getDescription() + "#" + "Implements control strategy: " + controlStrategy.getName() + "\n";
    }

    private void updateControlStrategyResults(ControlStrategy controlStrategy, EmfDataset dataset) throws EmfException {
        StrategyResult[] strategyResults = controlStrategy.getStrategyResults();
        strategyResults[0].setControlledInventoryDataset(dataset);
        ControlStrategyDAO dao = new ControlStrategyDAO();
        Session session = HibernateSessionFactory.get().getSession();
        try {
            dao.update(controlStrategy, session);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }

    }

    private Version version(EmfDataset inputDataset, int datasetVersion) {
        Session session = HibernateSessionFactory.get().getSession();
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
            throw new EmfException("Could not update inventory table '" + outputTable + "' using detail result table '"
                    + detailResultTable + "'");
        }

    }

    // FIXME: orl specfic column
    // TODO device code
    private String updateQuery(String outputTable, String detailResultTable, Datasource datasource) {
        return "UPDATE " + qualifiedTable(outputTable, datasource) + " SET " + "ann_emis=b.final_emissions,"
                + "ceff=b.control_eff," + "reff=b.rule_eff," + "rpen=b.rule_pen" + " FROM (SELECT "
                + "final_emissions, control_eff, rule_eff, rule_pen, source_id" + " FROM "
                + qualifiedTable(detailResultTable, datasource) + ") as b " + " WHERE "
                + qualifiedTable(outputTable, datasource) + ".record_id=b.source_id";
    }

    private String detailDatasetTable(ControlStrategy controlStrategy) {
        StrategyResult[] strategyResults = controlStrategy.getStrategyResults();
        Dataset detailedResultDataset = strategyResults[0].getDetailedResultDataset();
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
        String end = "Finished creating a controlled inventory";
        Status status = status(user, end);
        statusServices.add(status);
    }

    private void failStatus(StatusDAO statusServices, String message) {
        String end = "Failed to create a controlled inventory: " + message;
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
