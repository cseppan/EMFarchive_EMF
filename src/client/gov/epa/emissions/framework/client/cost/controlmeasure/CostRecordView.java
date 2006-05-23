package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.CostRecord;

public interface CostRecordView {
    void display(ControlMeasure measure, CostRecord record);
    
    CostRecord costRecord();

    void observe(CostRecordPresenter presenter);
    
}
