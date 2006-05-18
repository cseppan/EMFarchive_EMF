package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

public class EditableCMCostTabPresenterImpl implements EditableCMSummaryTabPresenter {

    private EditableCostsTabView view;

    private ControlMeasure measure;

    public EditableCMCostTabPresenterImpl(ControlMeasure measure, EditableCostsTabView view) {
        this.measure = measure;
        this.view = view;
    }

    public void doSave() throws EmfException {
        view.save(measure);
    }

}
