package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public class EditQAStepPresenter {

    private EditQAStepView view;

    private EmfDataset dataset;

    private EditableQATabView tabView;

    public EditQAStepPresenter(EditQAStepView view, EmfDataset dataset, EditableQATabView tabView) {
        this.view = view;
        this.tabView = tabView;
        this.dataset = dataset;
    }

    public void display(QAStep step) {
        view.observe(this);
        view.display(step, dataset);
    }

    public void doClose() {
        view.close();
    }

    public void doEdit() {
        tabView.refresh();
        doClose();
    }

}
