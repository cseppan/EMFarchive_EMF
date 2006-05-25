package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

public class EditableCMEfficiencyTabPresenterImpl implements ControlMeasureTabPresenter {

    private EditableEfficiencyTabView view;

    private ControlMeasure measure;

    public EditableCMEfficiencyTabPresenterImpl(ControlMeasure measure, EditableEfficiencyTabView view) {
        this.measure = measure;
        this.view = view;
    }

    public void doSave() throws EmfException {
        view.save(measure);
    }

}
