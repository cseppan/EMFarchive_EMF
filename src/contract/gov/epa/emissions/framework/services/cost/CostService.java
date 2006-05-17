package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;

public interface CostService {
    
        ControlMeasure[] getMeasures() throws EmfException;
        
        void addMeasure(ControlMeasure measure) throws EmfException;
        
        ControlMeasure updateMeasure(ControlMeasure measure) throws EmfException;
        
        ControlMeasure releaseLockedControlMeasure(ControlMeasure locked) throws EmfException;
        
        void removeMeasure(ControlMeasure measure) throws EmfException;
        
        ControlMeasure obtainLockedMeasure(User user, ControlMeasure measure) throws EmfException;
}
