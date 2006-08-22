package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public class EditEfficiencyRecordPresenter extends EfficiencyRecordPresenter {

    private EditEfficiencyRecordView view;

    public EditEfficiencyRecordPresenter(ControlMeasureEfficiencyTabView parentView, EditEfficiencyRecordView view) {
        super(parentView);
        this.view = view;
        
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
