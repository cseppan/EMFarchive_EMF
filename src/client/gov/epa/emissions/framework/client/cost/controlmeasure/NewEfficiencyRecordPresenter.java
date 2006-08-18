package gov.epa.emissions.framework.client.cost.controlmeasure;

import java.util.Date;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public class NewEfficiencyRecordPresenter {

    private EfficiencyRecordView view;

    private EditableEfficiencyTabView parentView;

    public NewEfficiencyRecordPresenter(EditableEfficiencyTabView parentView, EfficiencyRecordView view) {
        this.view = view;
        this.parentView = parentView;
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

    public void checkForDuplicates(EfficiencyRecord record) throws EmfException {
        EfficiencyRecord[] records = parentView.records();
        for (int i = 0; i < records.length; i++) {
            if (same(record, records[i])) {
                throw new EmfException("Duplicate record: " + duplicateRecordMsg());
            }
        }

    }

    private String duplicateRecordMsg() {
        return "The combination of 'Pollutant', 'Locale', 'Effective Date' and 'Existing Measure' should be unique";
    }

    private boolean same(EfficiencyRecord record1, EfficiencyRecord record2) {
        return record1.getPollutant().equals(record2.getPollutant()) && record1.getLocale().equals(record2.getLocale())
                && sameEffectiveDate(record1, record2)
                && record1.getExistingMeasureAbbr().equals(record2.getExistingMeasureAbbr());

    }

    private boolean sameEffectiveDate(EfficiencyRecord record1, EfficiencyRecord record2) {
        Date effectiveDate1 = record1.getEffectiveDate();
        Date effectiveDate2 = record2.getEffectiveDate();
        // if both are null mean user didn't enter a effective date=>equal
        if (effectiveDate1 == null && effectiveDate2 == null)
            return true;
        // if either one is null =>not equal
        if (effectiveDate1 == null || effectiveDate2 == null)
            return false;

        return effectiveDate1.equals(effectiveDate2);
    }

}
