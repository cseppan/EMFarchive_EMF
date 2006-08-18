package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public class NewEfficiencyRecordPresenter extends EfficiencyRecordPresenter {


    private NewEfficiencyRecordView view;

    public NewEfficiencyRecordPresenter(EditableEfficiencyTabView parentView, NewEfficiencyRecordView view) {
        super(parentView);
        this.view = view;
    }

    public void display(ControlMeasure measure, int noOfEfficiencyRecords) {
        view.observe(this);
        view.display(measure, newRecord(noOfEfficiencyRecords));
    }

    private EfficiencyRecord newRecord(int noOfEfficiencyRecords) {
        EfficiencyRecord efficiencyRecord = new EfficiencyRecord();
        efficiencyRecord.setRecordId(++noOfEfficiencyRecords);
        return efficiencyRecord;
    }

    public void addNew(EfficiencyRecord record) {
        parentView.add(record);
    }

}
