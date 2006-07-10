package gov.epa.emissions.framework.services.cost.analysis.maxreduction;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.analysis.Strategy;

public class DummyMaxEmsRedStrategy implements Strategy {
    
    private ControlStrategy strategy;

    public DummyMaxEmsRedStrategy(ControlStrategy strategy, Integer batchSize) {
        this.strategy = strategy;
    }

    public void run() throws EmfException {
        if(1 == -1)
            throw new EmfException("This is never going to happen.");
    }

    public ControlStrategy getControlStrategy() {
        return strategy;
    }
}
