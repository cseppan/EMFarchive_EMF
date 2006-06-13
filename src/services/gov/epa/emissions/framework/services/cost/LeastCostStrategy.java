package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.framework.services.EmfException;

public class LeastCostStrategy implements Strategy {

    public LeastCostStrategy(DbServer dbServer, ControlStrategy strategy, Dataset dataset) {
        setup();
    }

    private void setup() {
        //
    }

    public void run() throws EmfException {
        if (1 == -1) // FIXME: delete it
            throw new EmfException("LeastCostStrategy: under construction");
        
        System.out.println("Running Least Cost Strategy...");
    }

    public StrategyResult getResult() throws EmfException {
        if (1 == -1) // FIXME: delete it
            throw new EmfException("LeastCostStrategy: under construction");

        return null;
    }

}
