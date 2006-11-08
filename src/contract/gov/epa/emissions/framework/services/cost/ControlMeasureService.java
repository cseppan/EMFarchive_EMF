package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EMFService;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.controlmeasure.Scc;
import gov.epa.emissions.framework.services.cost.data.ControlTechnology;

public interface ControlMeasureService extends EMFService {

    ControlMeasure[] getMeasures() throws EmfException;

    void addMeasure(ControlMeasure measure, Scc[] sccs) throws EmfException;

    ControlMeasure updateMeasure(ControlMeasure measure, Scc[] sccs) throws EmfException;

    ControlMeasure releaseLockedControlMeasure(ControlMeasure locked) throws EmfException;

    void removeMeasure(ControlMeasure measure) throws EmfException;

    ControlMeasure obtainLockedMeasure(User user, ControlMeasure measure) throws EmfException;

    Scc[] getSccs(ControlMeasure measure) throws EmfException;

    ControlTechnology[] getControlTechnologies() throws EmfException;
    
    CostYearTable getCostYearTable(int targetYear) throws EmfException;

    ControlMeasure[] getMeasures(Pollutant pollutant) throws EmfException;
    
}
