package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.services.EmfException;

public interface CostService {
    
        ControlMeasure[] getMeasures() throws EmfException;
}
