package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

public class EditableCMCostTabPresenterImpl implements ControlMeasureTabPresenter {

    private EditableCostsTabView view;

    public EditableCMCostTabPresenterImpl(EditableCostsTabView view) {
        this.view = view;
    }

    public void doSave(ControlMeasure measure) throws EmfException {
        view.save(measure);
    }

}
