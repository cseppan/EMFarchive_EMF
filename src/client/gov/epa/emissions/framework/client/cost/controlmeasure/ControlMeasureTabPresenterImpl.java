package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

public class ControlMeasureTabPresenterImpl implements ControlMeasureTabPresenter {

    private EditableCMTabView view;

    public ControlMeasureTabPresenterImpl(EditableCMTabView view) {
        this.view = view;
    }

    public void doSave(ControlMeasure measure) throws EmfException {
        view.save(measure);
    }

}
