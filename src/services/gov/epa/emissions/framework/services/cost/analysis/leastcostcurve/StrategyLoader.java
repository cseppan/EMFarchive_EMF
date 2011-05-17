package gov.epa.emissions.framework.services.cost.analysis.leastcostcurve;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.analysis.leastcost.LeastCostAbstractStrategyLoader;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.Date;

public class StrategyLoader extends LeastCostAbstractStrategyLoader {
    
    public StrategyLoader(User user, DbServerFactory dbServerFactory, 
            HibernateSessionFactory sessionFactory, ControlStrategy controlStrategy) throws EmfException {
        super(user, dbServerFactory, 
                sessionFactory, controlStrategy);
    }

    public ControlStrategyResult loadStrategyResult(ControlStrategyInputDataset controlStrategyInputDataset) throws Exception {
        EmfDataset inputDataset = controlStrategyInputDataset.getInputDataset();

        //make sure inventory has the target pollutant, if not show a warning message
        if (!inventoryHasTargetPollutant(controlStrategyInputDataset)) {
            setStatus("Error processing input dataset: " + controlStrategyInputDataset.getInputDataset().getName() + ". Target pollutant, " + controlStrategy.getTargetPollutant().getName() + ", is not in the inventory.");
//            throw new EmfException("Error processing input dataset: " + controlStrategyInputDataset.getInputDataset().getName() + ". Target pollutant, " + controlStrategy.getTargetPollutant().getName() + ", is not in the inventory.");
        }
        //reset counters
        recordCount = 0;
        totalCost = 0.0;
        totalReduction = 0.0;
        
        //setup result
        if (controlStrategy.getDeleteResults() || results.length == 0)
            populateWorksheet(controlStrategyInputDataset);
        else {
            for (ControlStrategyResult result : results) {
                if (result.getStrategyResultType().getName().equals(StrategyResultType.leastCostControlMeasureWorksheet)) {
                    leastCostCMWorksheetResult = result;
                } else if (result.getStrategyResultType().getName().equals(StrategyResultType.leastCostCurveSummary)) {
                    leastCostCurveSummaryResult = result;
                }
            }
        }
        
        //also get the uncontrolled emission...
        uncontrolledEmis = getUncontrolledEmission(controlStrategyInputDataset);
        maxEmisReduction = getMaximumEmissionReduction(leastCostCMWorksheetResult);

        Double pctRedIncrement = controlStrategy.getConstraint().getDomainWidePctReductionIncrement();
        Double pctRed = 0.0;
        Double pctRedStart = controlStrategy.getConstraint().getDomainWidePctReductionStart();
        if (pctRedStart == null || pctRedStart == 0) pctRedStart = pctRedIncrement;
        Double pctRedEnd = controlStrategy.getConstraint().getDomainWidePctReductionEnd();
        if (pctRedEnd == null) pctRedEnd = 100.0;
        if (maxEmisReduction > 0) if (pctRedEnd > maxEmisReduction / uncontrolledEmis * 100) pctRedEnd = maxEmisReduction / uncontrolledEmis * 100;
        for (pctRed = pctRedStart; pctRed < pctRedEnd + pctRedIncrement; pctRed += pctRedIncrement) {

            setStatus("Populating the " + pctRed + " percent target detailed result.");

            ControlStrategyResult result = createStrategyResult(pctRed, inputDataset, controlStrategyInputDataset.getVersion());
            populateDetailedResult(controlStrategyInputDataset, result, uncontrolledEmis * pctRed / 100);

            //still need to calculate the total cost and reduction...
            //setResultTotalCostTotalReductionAndCount(result); //NOTE: we don't need to calculate the total cost and reduction
//            result.setRecordCount(recordCount);
            result.setTotalCost(null);
            result.setTotalReduction(null);
            setResultCount(result);

            //add summary information as keywords to the detailed result dataset
            addDetailedResultSummaryDatasetKeywords((EmfDataset)result.getDetailedResultDataset(), uncontrolledEmis * pctRed / 100);
            
            //finalize status
            result.setCompletionTime(new Date());
            result.setRunStatus("Completed.");
            saveControlStrategyResult(result);

            //create strategy messages result
            strategyMessagesResult = createStrategyMessagesResult("pct_" + pctRed, inputDataset, controlStrategyInputDataset.getVersion());
            populateStrategyMessagesDataset(controlStrategyInputDataset, strategyMessagesResult, result);
            setResultCount(strategyMessagesResult);
            
            //if the messages dataset is empty (no records) then remove the dataset and strategy result, there
            //is no point and keeping it around.
            if (strategyMessagesResult.getRecordCount() == 0) {
                deleteStrategyMessageResult(strategyMessagesResult);
                //set it null, so it referenced later it will be known that it doesn't exist...
                strategyMessagesResult = null;
            } else {
                strategyMessagesResult.setCompletionTime(new Date());
                strategyMessagesResult.setRunStatus("Completed.");
                saveControlStrategyResult(strategyMessagesResult);
            }

        }
        
        return null;
    }
}
