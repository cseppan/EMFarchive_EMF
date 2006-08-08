package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.orl.ORLNonPointFileFormat;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.sql.SQLException;

public class ControlStrategyInventoryOutput {

    private User user;

    private ControlStrategy controlStrategy;

    public ControlStrategyInventoryOutput(User user, ControlStrategy controlStrategy) {
        this.user = user;
        this.controlStrategy = controlStrategy;
    }

    public void create() throws Exception {
        EmfDbServer server = new EmfDbServer();
        EmfDataset[] inputDatasets = controlStrategy.getInputDatasets();

        for (int i = 0; i < inputDatasets.length; i++) {
            InternalSource[] sources = inputDatasets[i].getInternalSources();
            if (sources.length > 1) {
                throw new EmfException(
                        "At this moment datasets with multiple tables are not supported for creating a inventory output");
            }
            String inputTable = sources[0].getTable();
            DatasetCreator creator = new DatasetCreator("CSINVEN_", controlStrategy, user, server
                    .getEmissionsDatasource());
            String outputInventoryTableName = creator.outputTableName();
            createTable(outputInventoryTableName, new TableCreator(server.getEmissionsDatasource()), server);
            copyDataFromOriginalTable(inputTable, outputInventoryTableName, server.getEmissionsDatasource());
            updateDataWithDetailDatasetTable(outputInventoryTableName, detailDatasetTable(controlStrategy), server
                    .getEmissionsDatasource());
            EmfDataset dataset = creator.addDataset(tableFormat(server), inputDatasets[i].getName());
            updateDatasetId(outputInventoryTableName, server.getEmissionsDatasource(), dataset.getId());
        }

        // cleaning up
        // disconnecting the server
        // dropping new table if the actions are not successful
    }

    private void updateDatasetId(String tableName, Datasource datasource, int id) throws EmfException {
        String query = "UPDATE " + qualifiedTable(tableName, datasource) + " SET dataset_id=" + id;
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Could not update dataset_id column in the new inventory table '" + tableName + "'");
        }

    }

    private TableFormat tableFormat(DbServer server) {
        return new VersionedTableFormat(new ORLNonPointFileFormat(server.getSqlDataTypes()), server.getSqlDataTypes());
    }

    private void updateDataWithDetailDatasetTable(String outputTable, String detailResultTable,
            Datasource emissionsDatasource) throws EmfException {
        String query = updateQuery(outputTable, detailResultTable, emissionsDatasource);
        System.out.println("query-"+query);
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
                + " FROM (SELECT " + "final_emissions,source_id" + " FROM " + qualifiedTable(detailResultTable,datasource) + ") as b " + " WHERE "
                + qualifiedTable(outputTable,datasource) + ".record_id=b.source_id";
    }

    private String detailDatasetTable(ControlStrategy controlStrategy) {
        StrategyResult[] strategyResults = controlStrategy.getStrategyResults();
        Dataset detailedResultDataset = strategyResults[0].getDetailedResultDataset();
        return detailedResultDataset.getInternalSources()[0].getTable();
    }

    private void copyDataFromOriginalTable(String inputTable, String outputInventoryTableName,
            Datasource emissionsDatasource) throws EmfException {
        String query = copyDataFromOriginalTableQuery(inputTable, outputInventoryTableName, emissionsDatasource);
        try {
            emissionsDatasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Error occured when copying data from " + inputTable + " to "
                    + outputInventoryTableName + "\n" + e.getMessage());
        }
    }

    private String copyDataFromOriginalTableQuery(String inputTable, String outputTable, Datasource datasource) {
        return "INSERT INTO " + qualifiedTable(outputTable, datasource) + " SELECT * FROM "
                + qualifiedTable(inputTable, datasource);
    }

    private String qualifiedTable(String table, Datasource datasource) {
        return datasource.getName() + "." + table;
    }

    private void createTable(String outputInventoryTableName, TableCreator creator, EmfDbServer server)
            throws EmfException {
        try {
            creator.create(outputInventoryTableName, tableFormat(server));
        } catch (Exception e) {
            throw new EmfException("Could not create table: " + outputInventoryTableName + "\n" + e.getMessage());
        }
    }

}
