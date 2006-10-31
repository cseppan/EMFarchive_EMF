package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.QAStepTemplate;
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
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.qa.QADAO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

public class ControlStrategyInventoryOutput {

    private ControlStrategy controlStrategy;

    private DatasetCreator creator;

    private TableFormat tableFormat;

    private StatusDAO statusServices;

    private User user;

    private HibernateSessionFactory sessionFactory;

    public ControlStrategyInventoryOutput(User user, ControlStrategy controlStrategy,
            HibernateSessionFactory sessionFactory) throws Exception {
        this.controlStrategy = controlStrategy;
        this.user = user;
        this.sessionFactory = HibernateSessionFactory.get();
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
            setQASteps();
            server.disconnect();
        }
        endStatus(statusServices);
    }

    private void copyAndUpdateData(EmfDbServer server, Datasource datasource, EmfDataset inputDataset,
            String inputTable, String outputInventoryTableName) throws EmfException {
        copyDataFromOriginalTable(inputTable, outputInventoryTableName, version(inputDataset, controlStrategy
                .getDatasetVersion()), inputDataset, datasource);

        ControlStrategyResult result = controlStrategyResults(controlStrategy);
        updateDataWithDetailDatasetTable(outputInventoryTableName, detailDatasetTable(result), server
                .getEmissionsDatasource());

        EmfDataset dataset = creator.addDataset(controlStrategy.getDatasetType(), description(inputDataset),
                tableFormat, inputDataset.getName(), datasource);
        updateDatasetIdAndVersion(outputInventoryTableName, server.getEmissionsDatasource(), dataset.getId());

        updateControlStrategyResults(result, dataset);
    }
    
    private void setQASteps() throws EmfException {
        ControlStrategyResult result = controlStrategyResults(controlStrategy);
        EmfDataset controlledDataset = (EmfDataset)result.getControlledInventoryDataset();
        QAStepTemplate[] templates = controlledDataset.getDatasetType().getQaStepTemplates();
        addQASteps(templates, controlledDataset);
    }
    
    private void addQASteps(QAStepTemplate[] templates, EmfDataset dataset) throws EmfException {
        List steps = new ArrayList();

        for (int i = 0; i < templates.length; i++) {
            QAStep step = new QAStep(templates[i], dataset.getDefaultVersion()); //FIXME: use current version
            step.setDatasetId(dataset.getId());
            steps.add(step);
        }
        
        updateSteps((QAStep[])steps.toArray(new QAStep[0]));
    }
    
    private void updateSteps(QAStep[] steps) throws EmfException {
        Session session = sessionFactory.getSession();
        QAStep[] nonExistingSteps = getNonExistingSteps(steps);
        
        try {
            QADAO dao = new QADAO();
            dao.updateQAStepsIds(nonExistingSteps, session);
            dao.update(nonExistingSteps, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not update QA Steps");
        } 
    }

    private QAStep[] getNonExistingSteps(QAStep[] steps) throws EmfException {
        List stepsList = new ArrayList();

        Session session = sessionFactory.getSession();
        try {
            QADAO dao = new QADAO();
            for (int i = 0; i < steps.length; i++) {
                if (!dao.exists(steps[i], session))
                    stepsList.add(steps[i]);
            }
        } catch (RuntimeException e) {
            throw new EmfException("Could not check QA Steps");
        } 
        
        
        return (QAStep[])stepsList.toArray(new QAStep[0]);
    }

    private String description(EmfDataset inputDataset) {
        return inputDataset.getDescription() + "#" + "Implements control strategy: " + controlStrategy.getName() + "\n";
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
