package gov.epa.emissions.framework.services.cost.analysis.projectFutureYearInventory;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.other.StrategyMessagesFileFormat;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyLoader;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.DatasetTypesDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Session;

public class StrategyLoader extends AbstractStrategyLoader {
    public StrategyLoader(User user, DbServerFactory dbServerFactory, 
            HibernateSessionFactory sessionFactory, ControlStrategy controlStrategy) throws EmfException {
        super(user, dbServerFactory, 
                sessionFactory, controlStrategy);
    }

    public ControlStrategyResult loadStrategyResult(ControlStrategyInputDataset controlStrategyInputDataset) throws Exception {
        EmfDataset inputDataset = controlStrategyInputDataset.getInputDataset();
        //make sure inventory has indexes created...
        makeSureInventoryDatasetHasIndexes(controlStrategyInputDataset);

        //reset counters
        recordCount = 0;
        totalCost = 0.0;
        totalReduction = 0.0;

        //create detailed strategy result
        ControlStrategyResult result = createStrategyResult(inputDataset, controlStrategyInputDataset.getVersion());
        
        populateInventory(controlStrategyInputDataset, result);
        
        //create strategy messages result
        strategyMessagesResult = createStrategyMessagesResult(inputDataset, controlStrategyInputDataset.getVersion());
        populateStrategyMessagesDataset(controlStrategyInputDataset, strategyMessagesResult);
        setResultCount(strategyMessagesResult);
        
        //if the messages dataset is empty (no records) then remove the dataset and strategy result, there
        //is no point and keeping it around.
        if (strategyMessagesResult.getRecordCount() == 0) {
            deleteStrategyMessageResult(strategyMessagesResult);
        } else {
            strategyMessagesResult.setCompletionTime(new Date());
            strategyMessagesResult.setRunStatus("Completed.");
            saveControlStrategyResult(strategyMessagesResult);
        }

        //do this after updating the previous result, else it will override it...
        //still need to set the record count...
        setResultCount(result);

        return result;
    }

    protected void deleteStrategyMessageResult(ControlStrategyResult strategyMessagesResult) throws EmfException {
        //get rid of strategy results...
        Session session = sessionFactory.getSession();
        try {
            EmfDataset[] ds = controlStrategyDAO.getResultDatasets(controlStrategy.getId(), strategyMessagesResult.getId(), session);
            
            //get rid of old strategy results...
            controlStrategyDAO.removeControlStrategyResult(controlStrategy.getId(), strategyMessagesResult.getId(), session);
            //delete and purge datasets
            controlStrategyDAO.removeResultDatasets(ds, user, session, dbServer);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new EmfException("Could not remove control strategy message result.");
        } finally {
            session.close();
        }
    }

    private void populateInventory(ControlStrategyInputDataset controlStrategyInputDataset, ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "";
        query = "SELECT public.run_project_future_year_inventory("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ", " + controlStrategyResult.getId() + ");";
        System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            System.out.println("SQLException runStrategyUsingSQLApproach");
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //
        }
    }

    private void populateStrategyMessagesDataset(ControlStrategyInputDataset controlStrategyInputDataset, ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "";
        query = "SELECT public.populate_project_future_year_inventory_strategy_messages("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ", " + controlStrategyResult.getId() + ");";
        System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            System.out.println("SQLException runStrategyUsingSQLApproach");
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
        } finally {
            //
        }
    }

    private ControlStrategyResult createStrategyMessagesResult(EmfDataset inventory, int inventoryVersion) throws Exception 
    {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        result.setInputDataset(inventory);
        result.setInputDatasetVersion(inventoryVersion);
        result.setDetailedResultDataset(createDataset(inventory));

        result.setStrategyResultType(getStrategyMessagesResultType());
        result.setStartTime(new Date());
        result.setRunStatus("Start processing strategy messages result");

        //persist result
        saveControlStrategyResult(result);

        return result;
    }

    private EmfDataset createDataset(EmfDataset inventory) throws Exception {

        
//        FileFormat fileFormat = new StrategyMessagesFileFormat(dbServer.getSqlDataTypes());
//        TableFormat tableFormat = new NonVersionedTableFormat(fileFormat, dbServer.getSqlDataTypes());
        DatasetType datasetType = getDatasetType("Strategy Messages (CSV)");
//        TableFormat tableFormat = new FileFormatFactory(dbServer).tableFormat(datasetType);
        TableFormat tableFormat = new VersionedTableFormat(new StrategyMessagesFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes());
        
//        TableFormat tableFormat = factory.tableFormat(fileFormat, dbServer.getSqlDataTypes());
        
        
        return creator.addDataset("DS_", 
                DatasetCreator.createDatasetName(inventory.getName() + "_strategy_msgs"), 
                datasetType, 
                tableFormat, 
//                new NonVersionedDataFormatFactory().tableFormat(new StrategyMessagesFileFormat(dbServer.getSqlDataTypes()), dbServer.getSqlDataTypes()), 
                strategyMessagesDatasetDescription());
    }
    
    private String strategyMessagesDatasetDescription() {
        return "#Strategy Messages\n" + 
            "#Implements control strategy: " + controlStrategy.getName() + "\n#";
        }

    private DatasetType getDatasetType(String name) {
        Session session = sessionFactory.getSession();
        try {
            return new DatasetTypesDAO().get(name, session);
        } finally {
            session.close();
        }
    }

    private StrategyResultType getStrategyMessagesResultType() throws EmfException {
        StrategyResultType resultType = null;
        Session session = sessionFactory.getSession();
        try {
            resultType = new ControlStrategyDAO().getStrategyResultType(StrategyResultType.strategyMessages, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get detailed strategy result type");
        } finally {
            session.close();
        }
        return resultType;
    }

    @Override
    protected void doBatchInsert(ResultSet resultSet) throws Exception {
        // NOTE Auto-generated method stub
        
    }
}
