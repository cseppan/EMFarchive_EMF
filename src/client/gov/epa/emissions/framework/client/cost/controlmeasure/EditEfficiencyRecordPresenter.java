package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public class EditEfficiencyRecordPresenter {

    private EditEfficiencyRecordView view;
    
    private EditableEfficiencyTabView parentView;

    public EditEfficiencyRecordPresenter(EditableEfficiencyTabView parentView, EditEfficiencyRecordView view) {
        this.view = view;
        this.parentView = parentView;
    }
    
    public void display(ControlMeasure measure, EfficiencyRecord record) {
        view.observe(this);
        view.display(measure, record);
    }
    
    public void doSave(ControlMeasure measure) throws EmfException {
        parentView.save(measure);
    }

    public void refresh() {
       parentView.refresh();
        
    }
    
}
