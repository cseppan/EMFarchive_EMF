package gov.epa.emissions.framework.services.cost.analysis.leastcost;

import java.sql.SQLException;
import java.util.Date;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.hibernate.Session;

public class StrategyLoader extends LeastCostAbstractStrategyLoader {
    
    public StrategyLoader(User user, DbServerFactory dbServerFactory, 
            HibernateSessionFactory sessionFactory, ControlStrategy controlStrategy) throws EmfException {
        super(user, dbServerFactory, 
                sessionFactory, controlStrategy);
    }

    public ControlStrategyResult loadStrategyResult(ControlStrategyInputDataset controlStrategyInputDataset) throws Exception {
        EmfDataset inputDataset = controlStrategyInputDataset.getInputDataset();

        //make sure inventory has the target pollutant, if not don't run
        if (!inventoryHasTargetPollutant(controlStrategyInputDataset)) {
            throw new EmfException("Error processing input dataset: " + controlStrategyInputDataset.getInputDataset().getName() + ". Target pollutant, " + controlStrategy.getTargetPollutant().getName() + ", is not in the inventory.");
        }
        //reset counters
        recordCount = 0;
        totalCost = 0.0;
        totalReduction = 0.0;
        
        //setup result
        ControlStrategyResult detailedResult = createStrategyResult(inputDataset, controlStrategyInputDataset.getVersion());
//        populateWorksheet(controlStrategyInputDataset);
        //setup result
//        if (controlStrategy.getDeleteResults() || results.length == 0)
            populateWorksheet(controlStrategyInputDataset);
//        else {
//            for (ControlStrategyResult result : results) {
//                if (result.getStrategyResultType().getName().equals(StrategyResultType.leastCostControlMeasureWorksheet)) {
//                    leastCostCMWorksheetResult = result;
//                }
//            }
//        }

        double targetEmissionReduction = getTargetEmissionReduction();
        populateDetailedResult(controlStrategyInputDataset, detailedResult, targetEmissionReduction);

        //create strategy messages result
        strategyMessagesResult = createStrategyMessagesResult(inputDataset, controlStrategyInputDataset.getVersion());
        populateStrategyMessagesDataset(controlStrategyInputDataset, strategyMessagesResult, detailedResult);
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

        
        //still need to calculate the total cost and reduction...
        setResultTotalCostTotalReductionAndCount(detailedResult);
        
        //also get the uncontrolled emission...
        uncontrolledEmis = getUncontrolledEmission(controlStrategyInputDataset);
        addDetailedResultSummaryDatasetKeywords((EmfDataset)detailedResult.getDetailedResultDataset(), targetEmissionReduction);
        return detailedResult;
    }

    // return ControlStrategies orderby name
    public Double getTargetEmissionReduction() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return (Double)session.createQuery("select cS.domainWideEmisReduction " +
                    "from ControlStrategyConstraint cS " +
                    "where cS.controlStrategyId = " + controlStrategy.getId()).uniqueResult();
        } catch (RuntimeException e) {
            throw new EmfException("Could not get strategy target emission reduction");
        } finally {
            session.close();
        }
    }

    private void populateStrategyMessagesDataset(ControlStrategyInputDataset controlStrategyInputDataset, ControlStrategyResult strategyMessagesResult, ControlStrategyResult detailedResult) throws EmfException {
        String query = "";
        query = "SELECT public.populate_max_emis_red_strategy_messages("  + controlStrategy.getId() + ", " + controlStrategyInputDataset.getInputDataset().getId() + ", " + controlStrategyInputDataset.getVersion() + ", " + strategyMessagesResult.getId() + ", " + detailedResult.getId() + ");";
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
}
