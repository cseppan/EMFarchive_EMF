package gov.epa.emissions.framework.services.cost;

import java.util.Date;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.data.StrategyResult;

public class MaxEmsRedStrategy implements Strategy {
    
    StrategyResult result;
    
    DbServer dbServer;
    
    ControlStrategy strategy;

    public MaxEmsRedStrategy(DbServer dbServer, ControlStrategy strategy) {
        this.dbServer = dbServer;
        this.strategy = strategy;
        
        setup();
    }

    private void setup() {
        result = new StrategyResult();
    }

    public void run() throws EmfException {
        if (1 == -1) //FIXME: delete it
            throw new EmfException("MaxEmsRedStrategy: under construction");
       
       result.setSourceId(1);
       result.setDatasetId(1);
       result.setControlMeasureID(1);
       result.setControlStrategy(strategy.getName());
       result.setPollutant(strategy.getMajorPollutant());
       result.setCost(Math.random()*100);
       result.setCostPerTon(Math.random()*10);
       result.setRedEmissions(Math.random()*1000);
       
       strategy.setTotalCost(Math.random()*100);
       strategy.setReduction(Math.random()*1000);
       strategy.setCompletionDate(new Date());
    }

    public StrategyResult getResult() {
        return result;
    }

    public ControlStrategy getControlStrategy() {
        return strategy;
    }
}
