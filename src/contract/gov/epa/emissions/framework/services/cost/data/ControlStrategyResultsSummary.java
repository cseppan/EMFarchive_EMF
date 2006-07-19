package gov.epa.emissions.framework.services.cost.data;

import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;

public class ControlStrategyResultsSummary {

    private StrategyResult[] strategyResults;
    
    public ControlStrategyResultsSummary(StrategyResult[] strategyResults){
        this.strategyResults = strategyResults;
    }

    public float getStrategyTotalCost() {
        float totalCost = 0;
        
        for (int i = 0; i < strategyResults.length; i++)
            totalCost += strategyResults[i].getTotalCost();
        
        return totalCost;
    }
}
