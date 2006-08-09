package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Dataset;
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
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.SQLException;

import org.hibernate.Session;

public class ControlStrategyInventoryOutput {

    private ControlStrategy controlStrategy;

    private DatasetCreator creator;

    private TableFormat tableFormat;

    public ControlStrategyInventoryOutput(User user, ControlStrategy controlStrategy) throws Exception {
        this.controlStrategy = controlStrategy;
        creator = new DatasetCreator("CSINVEN_", controlStrategy, user);
        this.tableFormat = new FileFormatFactory().tableFormat(controlStrategy.getDatasetType());
    }

    public void create() throws Exception {
        EmfDbServer server = new EmfDbServer();
        Datasource datasource = server.getEmissionsDatasource();
        TableCreator tableCreator = new TableCreator(datasource);
        EmfDataset inputDataset = controlStrategy.getInputDatasets()[0];
        String inputTable = inputTable(inputDataset);
        String outputInventoryTableName = creator.outputTableName();
        createTable(outputInventoryTableName, tableCreator);
        try {
            copyDataFromOriginalTable(inputTable, outputInventoryTableName, version(inputDataset, controlStrategy
                    .getDatasetVersion()), datasource);

            updateDataWithDetailDatasetTable(outputInventoryTableName, detailDatasetTable(controlStrategy), server
                    .getEmissionsDatasource());

            EmfDataset dataset = creator.addDataset(tableFormat, datasource, inputDataset.getName());
            updateDatasetIdAndVersion(outputInventoryTableName, server.getEmissionsDatasource(), dataset.getId());
        } catch (Exception e) {
            tableCreator.drop(qualifiedTable(outputInventoryTableName, datasource));
            throw e;
        } finally {
            server.disconnect();

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
    private String updateQuery(String outputTable, String detailResultTable, Datasource datasource) {
        return "UPDATE " + qualifiedTable(outputTable, datasource) + " SET " + "ann_emis=b.final_emissions"
                + " FROM (SELECT " + "final_emissions,source_id" + " FROM "
                + qualifiedTable(detailResultTable, datasource) + ") as b " + " WHERE "
                + qualifiedTable(outputTable, datasource) + ".record_id=b.source_id";
    }

    private String detailDatasetTable(ControlStrategy controlStrategy) {
        StrategyResult[] strategyResults = controlStrategy.getStrategyResults();
        Dataset detailedResultDataset = strategyResults[0].getDetailedResultDataset();
        return detailedResultDataset.getInternalSources()[0].getTable();
    }

    private void copyDataFromOriginalTable(String inputTable, String outputInventoryTableName, Version version,
            Datasource emissionsDatasource) throws EmfException {
        String query = copyDataFromOriginalTableQuery(inputTable, outputInventoryTableName, version,
                emissionsDatasource);
        try {
            emissionsDatasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Error occured when copying data from " + inputTable + " to "
                    + outputInventoryTableName + "\n" + e.getMessage());
        }
    }

    private String copyDataFromOriginalTableQuery(String inputTable, String outputTable, Version version,
            Datasource datasource) {
        String versionedQuery = new VersionedDatasetQuery(version).generate(qualifiedTable(inputTable, datasource));

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

}
