package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.CostRecord;

public interface EditableCostsTabView {
    void save(ControlMeasure measure) throws EmfException;
    
    void add(CostRecord record);
    
    void edit(CostRecord record);
}
