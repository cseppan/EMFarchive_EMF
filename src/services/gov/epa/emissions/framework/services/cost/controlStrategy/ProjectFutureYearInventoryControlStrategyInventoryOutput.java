package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.SQLException;

public class ProjectFutureYearInventoryControlStrategyInventoryOutput extends AbstractControlStrategyInventoryOutput {

    public ProjectFutureYearInventoryControlStrategyInventoryOutput(User user, ControlStrategy controlStrategy,
            ControlStrategyResult controlStrategyResult, HibernateSessionFactory sessionFactory, 
            DbServerFactory dbServerFactory) throws Exception {
        super(user, controlStrategy,
                controlStrategyResult, sessionFactory, 
                dbServerFactory);
    }

    public void create() throws Exception {
        doCreateInventory(inputDataset, getDatasetTableName(inputDataset));
    }

    protected void doCreateInventory(EmfDataset inputDataset, String inputTable) throws EmfException, Exception, SQLException {
        startStatus(statusServices);
        try {
            EmfDataset dataset = creator.addDataset(creator.createDatasetName(inputDataset + "_CntlInv"), 
                    inputDataset, inputDataset.getDatasetType(), 
                    tableFormat, description(inputDataset));
            
            String outputInventoryTableName = getDatasetTableName(dataset);
            
            ControlStrategyResult result = getControlStrategyResult(controlStrategyResult.getId());
            createDetailedResultTableIndexes(result);
            createControlledInventory(dataset.getId(), inputTable, detailDatasetTable(result),
                    outputInventoryTableName, version(inputDataset, controlStrategyResult.getInputDatasetVersion()),
                    inputDataset, datasource);

            setControlStrategyResultContolledInventory(result, dataset);
        } catch (Exception e) {
            failStatus(statusServices, e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
//            setandRunQASteps();
            dbServer.disconnect();
        }
        
    }

    private void createControlledInventory(int datasetId, String inputTable, String detailResultTable, String outputTable, Version version,
            Dataset dataset, Datasource datasource) throws EmfException {
        String query = "SELECT public.create_projected_future_year_inventory("  + controlStrategy.getId() + ", " + inputDataset.getId() + ", " + version.getVersion() + ", " + controlStrategyResult.getId() + ", " + datasetId + ");";

        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Error occured when copying data from " + inputTable + " to "
                    + outputTable + "\n" + e.getMessage());
        }
    }
}
