package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;
import gov.epa.emissions.framework.services.data.EmfDateFormat;

public class EditEfficiencyRecordWindow extends EfficiencyRecordWindow implements EditEfficiencyRecordView {

    private EditEfficiencyRecordPresenter presenter;

    public EditEfficiencyRecordWindow(ManageChangeables changeablesList, DesktopManager desktopManager,
            EmfSession session, CostYearTable costYearTable) {
        super("Edit Efficiency Record", changeablesList, desktopManager, session, costYearTable);
    }

    public void display(ControlMeasure measure, EfficiencyRecord record) {
        super.display(measure, record);
        populateFields();
    }

    private void populateFields() {
        pollutant.setSelectedItem(record.getPollutant());
        equationType.setSelectedItem(record.getEquationType());
        efficiency.setText(record.getEfficiency() + "");
        costYear.setText(record.getCostYear() + "");
        costperTon.setText(record.getCostPerTon() + "");
        refYrCostPerTon.setText(record.getRefYrCostPerTon() + "");
        locale.setText(record.getLocale());
        ruleEffectiveness.setText(record.getRuleEffectiveness() + "");
        rulePenetration.setText(record.getRulePenetration() + "");
        caprecFactor.setText(record.getCapRecFactor() + "");
        discountRate.setText(record.getDiscountRate() + "");
        detail.setText(record.getDetail());
        effectiveDate.setText(formatEffectiveDate());
        measureAbbreviation.setText(record.getExistingMeasureAbbr());
        existingdevCode.setText(record.getExistingDevCode() + "");
        lastModifiedBy.setText(record.getLastModifiedBy() + "");
        lastModifiedTime.setText(EmfDateFormat.format_MM_DD_YYYY_HH_mm(record.getLastModifiedTime()));
    }

    public void save() {
        try {
            messagePanel.clear();
            doSave();
            updateControlMeasureEfficiencyTab(record);
            presenter.update(record);
            presenter.refresh();
            disposeView();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
            return;
        }
    }

    private void updateControlMeasureEfficiencyTab(EfficiencyRecord record) throws EmfException {
        presenter.checkForDuplicate(record);
    }

    public void observe(EditEfficiencyRecordPresenter presenter) {
        this.presenter = presenter;

    }

}