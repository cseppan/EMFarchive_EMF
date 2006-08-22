package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public interface ControlMeasureEfficiencyTabView extends ControlMeasureTabView {
    
    void add(EfficiencyRecord record);

    void refresh();

    EfficiencyRecord[] records();
}
