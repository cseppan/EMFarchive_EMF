package gov.epa.emissions.framework.services.cost.data;

import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;

import java.io.Serializable;

public class ControlStrategyResultsSummary implements Serializable {

    private int id;
    
    private StrategyResult[] strategyResults;
    
    public ControlStrategyResultsSummary(){
        //
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public StrategyResult[] getStrategyResults() {
        return strategyResults;
    }

    public void setStrategyResults(StrategyResult[] strategyResults) {
        this.strategyResults = strategyResults;
    }
    
}
