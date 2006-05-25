package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public class EfficiencyRecordPresenter {

    private EfficiencyRecordView view;
    
    private EditableEfficiencyTabView parentView;

    public EfficiencyRecordPresenter(EditableEfficiencyTabView parentView, EfficiencyRecordView view) {
        this.view = view;
        this.parentView = parentView;
    }
    
    public void display(ControlMeasure measure, EfficiencyRecord record) {
        view.observe(this);
        view.display(measure, record);
    }
    
    public void addNew(EfficiencyRecord record) {
        parentView.add(record);
    }
    
}
