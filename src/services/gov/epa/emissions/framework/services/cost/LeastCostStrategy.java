package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.data.StrategyResult;

public class LeastCostStrategy implements Strategy {

    public LeastCostStrategy(DbServer dbServer, ControlStrategy strategy) {
        setup();
    }

    private void setup() {
        //
    }

    public void run() throws EmfException {
        //if (1 == -1) // FIXME: delete it
            throw new EmfException("LeastCostStrategy: under construction");
        
        //System.out.println("Running Least Cost Strategy...");
    }

    public StrategyResult getResult() {

        return null;
    }

    public ControlStrategy getControlStrategy() {
        // NOTE Auto-generated method stub
        return null;
    }

}
