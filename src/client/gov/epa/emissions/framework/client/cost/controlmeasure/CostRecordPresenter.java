package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.CostRecord;

public class CostRecordPresenter {

    private CostRecordView view;
    
    private EditableCostsTabView parentView;

    public CostRecordPresenter(EditableCostsTabView parentView, CostRecordView view) {
        this.view = view;
        this.parentView = parentView;
    }
    
    public void display(ControlMeasure measure) {
        view.observe(this);
        view.display(measure);
    }
    
    public void addNew(CostRecord record) {
        parentView.add(record);
    }
}
