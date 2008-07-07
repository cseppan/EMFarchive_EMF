package gov.epa.emissions.framework.services.cost.analysis.projectFutureYearInventory;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyLoader;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.DatasetCreator;
import gov.epa.emissions.framework.services.cost.controlStrategy.FileFormatFactory;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
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
        
        ControlStrategyResult result = createProjectedFutureYearInventoryResult(inputDataset, controlStrategyInputDataset.getVersion());
        
        populateInventory(controlStrategyInputDataset, result);

        System.out.println(System.currentTimeMillis() + " done with");

        //still need to set the record count...
        setResultCount(result);
        return result;
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
            //3
        }
    }

    protected ControlStrategyResult createProjectedFutureYearInventoryResult(EmfDataset inventory, int inventoryVersion) throws Exception 
    {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        result.setInputDataset(inventory);
        result.setInputDatasetVersion(inventoryVersion);
        result.setDetailedResultDataset(createDataset(inventory));

        result.setStrategyResultType(getProjectedFutureYearInventoryResultType());
        result.setStartTime(new Date());
        result.setRunStatus("Start processing project future year inventory result");

        //persist result
        saveControlStrategyResult(result);

        return result;
    }

    private EmfDataset createDataset(EmfDataset inventory) throws Exception {
        return creator.addDataset("DS_", 
                DatasetCreator.createDatasetName(inventory.getName() + "_project"), 
                inventory.getDatasetType(), 
                new FileFormatFactory(dbServer).tableFormat(inventory.getDatasetType()), 
                inventory.getDescription());
    }

    private StrategyResultType getProjectedFutureYearInventoryResultType() throws EmfException {
        StrategyResultType resultType = null;
        Session session = sessionFactory.getSession();
        try {
            resultType = new ControlStrategyDAO().getStrategyResultType(StrategyResultType.annotatedInventoryResult, session);
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
