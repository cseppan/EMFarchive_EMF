package gov.epa.emissions.framework.services.cost.analysis.leatcost;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

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
        
    }

    public ControlStrategyResult getResult() {

        return null;
    }

    public ControlStrategy getControlStrategy() {
        // NOTE Auto-generated method stub
        return null;
    }

    public void close() {
        // NOTE Auto-generated method stub
        
    }

}
