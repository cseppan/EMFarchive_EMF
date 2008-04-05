package gov.epa.emissions.framework.services.cost.analysis.leastcost;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.common.AbstractStrategyLoader;
import gov.epa.emissions.framework.services.cost.analysis.common.StrategyLeastCostCMWorksheetTableFormat;
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
    
    private ControlStrategyResult leastCostCMWorksheetResult;

    public StrategyLoader(User user, DbServerFactory dbServerFactory, 
            HibernateSessionFactory sessionFactory, ControlStrategy controlStrategy, 
            int batchSize, boolean useSQLApproach) throws EmfException {
        super(user, dbServerFactory, 
                sessionFactory, controlStrategy, 
                batchSize, useSQLApproach);
    }

    public StrategyLoader(User user, DbServerFactory dbServerFactory, 
            HibernateSessionFactory sessionFactory, ControlStrategy controlStrategy, 
            int batchSize) throws EmfException {
        super(user, dbServerFactory, 
                sessionFactory, controlStrategy, 
                batchSize);
    }

    public ControlStrategyResult loadLeastCostCMWorksheetResult() throws EmfException {
        this.leastCostCMWorksheetResult = createLeastCostCMWorksheetResult();
        return this.leastCostCMWorksheetResult;
    }
    
    public ControlStrategyResult loadStrategyResult(ControlStrategyInputDataset controlStrategyInputDataset) throws Exception {
        EmfDataset inputDataset = controlStrategyInputDataset.getInputDataset();
        //make sure inventory has indexes created...
        makeSureInventoryDatasetHasIndexes(controlStrategyInputDataset);
        //make sure inventory has the target pollutant, if not don't run
        if (!inventoryHasTargetPollutant(controlStrategyInputDataset)) {
            throw new EmfException("Error processing input dataset: " + controlStrategyInputDataset.getInputDataset().getName() + ". Target pollutant, " + controlStrategy.getTargetPollutant().getName() + ", is not in the inventory.");
        }
        //reset counters
        recordCount = 0;
        totalCost = 0.0;
        totalReduction = 0.0;
        
        //setup result
        ControlStrategyResult result = createStrategyResult(inputDataset, controlStrategyInputDataset.getVersion());
        runStrategyUsingSQLApproach(controlStrategyInputDataset, result);
        System.out.println(System.currentTimeMillis() + " done with");
        //still need to calculate the total cost and reduction...
        setResultTotalCostTotalReductionAndCount(result);
        return result;
    }

    protected void doBatchInsert(ResultSet resultSet) throws Exception {
        //
    }

    private EmfDataset createDataset() throws EmfException {
        //"LeatCostCM_", 
        return creator.addDataset("CSLCM_", 
                DatasetCreator.createDatasetName(controlStrategy.getName() + "_MeasureWorksheet"), getControlStrategyLeastCostCMWorksheetDatasetType(), 
                new StrategyLeastCostCMWorksheetTableFormat(dbServer.getSqlDataTypes()), leastCostCMWorksheetDescription());
    }

    private String leastCostCMWorksheetDescription() {
        return "#Control strategy least cost control measure worksheet\n" + 
            "#Implements control strategy: " + controlStrategy.getName() + "\n#";
        }

    private DatasetType getControlStrategyLeastCostCMWorksheetDatasetType() {
        Session session = sessionFactory.getSession();
        try {
            return new DatasetTypesDAO().get("Control Strategy Least Cost Control Measure Worksheet", session);
        } finally {
            session.close();
        }
    }

    private ControlStrategyResult createLeastCostCMWorksheetResult() throws EmfException {
        ControlStrategyResult result = new ControlStrategyResult();
        result.setControlStrategyId(controlStrategy.getId());
        result.setDetailedResultDataset(createDataset());

        result.setStrategyResultType(getLeastCostCMWorksheetResultType());
        result.setStartTime(new Date());
        result.setRunStatus("Start processing LeastCost CM Worksheet result");

        //persist result
        saveControlStrategyResult(result);
        
        //create indexes on the datasets table...
        createLeastCostCMWorksheetIndexes((EmfDataset)result.getDetailedResultDataset());

        return result;
    }

    private StrategyResultType getLeastCostCMWorksheetResultType() throws EmfException {
        StrategyResultType resultType = null;
        Session session = sessionFactory.getSession();
        try {
            resultType = new ControlStrategyDAO().getStrategyResultType("Least Cost Control Measure Worksheet", session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get detailed strategy result type");
        } finally {
            session.close();
        }
        return resultType;
    }

    private void runStrategyUsingSQLApproach(ControlStrategyInputDataset controlStrategyInputDataset, ControlStrategyResult controlStrategyResult) throws EmfException {
        String query = "";
        query = "SELECT public.run_least_cost_strategy("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ", " + controlStrategyResult.getId() + ");";
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

    public void createLeastCostCMWorksheetIndexes(EmfDataset leastCostCMWorksheetDataset) {
        String query = "SELECT public.create_least_cost_worksheet_table_indexes('" + emissionTableName(leastCostCMWorksheetDataset).toLowerCase() + "')";
        System.out.println(System.currentTimeMillis() + " " + query);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            //supress all errors, the indexes might already be on the table...
        } finally {
            //
        }
    }

}
