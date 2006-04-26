package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

public class EditableCMSummaryTabPresenterImpl implements EditableCMSummaryTabPresenter {

    private EditableCMSummaryTabView view;

    private ControlMeasure measure;

    public EditableCMSummaryTabPresenterImpl(ControlMeasure measure, EditableCMSummaryTabView view) {
        this.measure = measure;
        this.view = view;
    }

    public void doSave() throws EmfException {
        view.save(measure);
    }

}
