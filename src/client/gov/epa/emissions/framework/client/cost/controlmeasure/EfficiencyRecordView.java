package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public interface EfficiencyRecordView {
    void display(ControlMeasure measure, EfficiencyRecord record);
    
    EfficiencyRecord efficiencyRecord();

    void observe(EfficiencyRecordPresenter presenter);
    
}
