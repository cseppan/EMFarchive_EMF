package gov.epa.emissions.framework.services.cost.analysis.projectFutureYearInventory;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyLoader;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class StrategyLoader extends AbstractStrategyLoader {
    public StrategyLoader(User user, DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory,
            ControlStrategy controlStrategy) throws EmfException {
        super(user, dbServerFactory, sessionFactory, controlStrategy);
    }

    public ControlStrategyResult loadStrategyResult(ControlStrategyInputDataset controlStrategyInputDataset)
            throws Exception {
        EmfDataset inputDataset = controlStrategyInputDataset.getInputDataset();
        // not needed, done in the beforeRun method.
        // //make sure inventory has indexes created...
        // makeSureInventoryDatasetHasIndexes(inputDataset);

        // reset counters
        recordCount = 0;
        totalCost = 0.0;
        totalReduction = 0.0;

        // create detailed strategy result
        ControlStrategyResult result = createStrategyResult(inputDataset, controlStrategyInputDataset.getVersion());

        populateInventory(controlStrategyInputDataset, result);

        // //create strategy messages result
        // strategyMessagesResult = createStrategyMessagesResult(inputDataset,
        // controlStrategyInputDataset.getVersion());
        // populateStrategyMessagesDataset(strategyMessagesResult);
        // setResultCount(strategyMessagesResult);
        //        
        // //if the messages dataset is empty (no records) then remove the dataset and strategy result, there
        // //is no point and keeping it around.
        // if (strategyMessagesResult.getRecordCount() == 0) {
        // deleteStrategyMessageResult(strategyMessagesResult);
        // //set it null, so it referenced later it will be known that it doesn't exist...
        // strategyMessagesResult = null;
        // } else {
        // strategyMessagesResult.setCompletionTime(new Date());
        // strategyMessagesResult.setRunStatus("Completed.");
        // saveControlStrategyResult(strategyMessagesResult);
        // }

        // do this after updating the previous result, else it will override it...
        // still need to set the record count...
        setResultCount(result);

        return result;
    }

    protected void populateMessageOutput() throws Exception {
        populateStrategyMessagesDataset(strategyMessagesResult);
        setResultCount(strategyMessagesResult);

        // if the messages dataset is empty (no records) then remove the dataset and strategy result, there
        // is no point and keeping it around.
        if (strategyMessagesResult.getRecordCount() == 0) {
            deleteStrategyMessageResult(strategyMessagesResult);
            // set it null, so it referenced later it will be known that it doesn't exist...
            strategyMessagesResult = null;
        } else {
            strategyMessagesResult.setCompletionTime(new Date());
            strategyMessagesResult.setRunStatus("Completed.");
            saveControlStrategyResult(strategyMessagesResult);
        }
    }

    private void populateInventory(ControlStrategyInputDataset controlStrategyInputDataset,
            ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "";
        query = "SELECT public.run_project_future_year_inventory(" + controlStrategy.getId() + ", "
                + controlStrategyInputDataset.getInputDataset().getId() + ", "
                + controlStrategyInputDataset.getVersion() + ", " + controlStrategyResult.getId() + ");";
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

    private void populateStrategyMessagesDataset(ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "";
        query = "SELECT public.populate_project_future_year_inventory_strategy_messages(" + controlStrategy.getId()
                + ", " + controlStrategyResult.getId() + ");";
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

    @Override
    protected void doBatchInsert(ResultSet resultSet) throws Exception {
        // NOTE Auto-generated method stub

    }
    
    public void addKeyVal(EmfDataset dataset, String keywordName, String value) {
        this.creator.addKeyVal(dataset, keywordName, value);
    }
    
    public void update(EmfDataset dataset) throws EmfException {
        this.creator.update(dataset);
    }
}
