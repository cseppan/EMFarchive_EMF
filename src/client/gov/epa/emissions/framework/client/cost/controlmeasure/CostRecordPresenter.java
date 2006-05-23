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
    
    public void display(ControlMeasure measure, CostRecord record) {
        view.observe(this);
        view.display(measure, record);
    }
    
    public void addNew(CostRecord record) {
        parentView.add(record);
    }
    
    public void doEdit(CostRecord record) {
        parentView.edit(record);
    }
}
