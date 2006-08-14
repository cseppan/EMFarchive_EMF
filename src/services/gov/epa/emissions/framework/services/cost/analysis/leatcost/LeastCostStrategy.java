package gov.epa.emissions.framework.services.cost.analysis.leatcost;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResult;

public class LeastCostStrategy implements Strategy {

    public LeastCostStrategy(DbServer dbServer, ControlStrategy strategy) {
        setup();
    }

    private void setup() {
        //
    }

    public void run(User user) throws EmfException {
        //if (1 == -1) // FIXME: delete it
            throw new EmfException("LeastCostStrategy: under construction");
        
    }

    public StrategyResult getResult() {

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
