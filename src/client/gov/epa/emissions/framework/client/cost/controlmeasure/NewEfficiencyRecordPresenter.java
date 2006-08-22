package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public class NewEfficiencyRecordPresenter extends EfficiencyRecordPresenter {

    private NewEfficiencyRecordView view;

    public NewEfficiencyRecordPresenter(ControlMeasureEfficiencyTabView parentView, NewEfficiencyRecordView view) {
        super(parentView);
        this.view = view;
    }

    public void display(ControlMeasure measure) {
        view.observe(this);
        view.display(measure, newRecord());
    }

    private EfficiencyRecord newRecord() {
        EfficiencyRecord efficiencyRecord = new EfficiencyRecord();
        efficiencyRecord.setRecordId(newRecordId());
        return efficiencyRecord;
    }

    private int newRecordId() {
        int maxRecordId = maxRecordId(parentView.records());
        
        return ++maxRecordId;
    }

    private int maxRecordId(EfficiencyRecord[] records) {
        int id = 0;
        for (int i = 0; i < records.length; i++) {
            if (records[i].getRecordId() > id)
                id = records[i].getRecordId();
        }
        return id;
    }

    public void addNew(EfficiencyRecord record) {
        parentView.add(record);
    }

}
