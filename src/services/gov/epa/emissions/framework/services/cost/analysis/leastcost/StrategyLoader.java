package gov.epa.emissions.framework.services.cost.analysis.leastcost;

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
        ControlStrategyResult detailedResult = createStrategyResult(inputDataset, controlStrategyInputDataset.getVersion());
        populateWorksheet(controlStrategyInputDataset);
        
        double targetEmissionReduction = getTargetEmissionReduction();
        populateDetailedResult(controlStrategyInputDataset, detailedResult, targetEmissionReduction);
        System.out.println(System.currentTimeMillis() + " done with");
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
}
