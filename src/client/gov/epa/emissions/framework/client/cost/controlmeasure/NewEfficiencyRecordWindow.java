package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.data.EfficiencyRecord;

public class NewEfficiencyRecordWindow extends EfficiencyRecordWindow implements NewEfficiencyRecordView {

    private NewEfficiencyRecordPresenter presenter;

    public NewEfficiencyRecordWindow(ManageChangeables changeablesList, DesktopManager desktopManager, EmfSession session) {
        super("Add Efficiency Record", changeablesList, desktopManager,session);
    }

    public void display(ControlMeasure measure, EfficiencyRecord record) {
        super.display(measure,record);
        setDefaults();
    }

    private void setDefaults() {
        existingdevCode.setText("");
        locale.setText("");
        measureAbbreviation.setText("");
        ruleEffectiveness.setText("100.0");
        rulePenetration.setText("100.0");
        equationType.setSelectedIndex(0);
    }

    private void updateControlMeasureEfficiencyTab(EfficiencyRecord record) throws EmfException {
        presenter.checkForDuplicate(record);
    }

    public void observe(NewEfficiencyRecordPresenter presenter) {
        this.presenter = presenter;
    }


    public void save() {
        try {
            messagePanel.clear();
            doSave();
            updateControlMeasureEfficiencyTab(record);
            presenter.addNew(record);
            disposeView();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
            return;
        }
        
    }


}
