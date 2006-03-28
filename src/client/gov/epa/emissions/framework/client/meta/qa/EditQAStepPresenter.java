package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public class EditQAStepPresenter {

    private EditQAStepView view;

    private EmfDataset dataset;

    private EditableQATabView tabView;

    private EmfSession session;

    public EditQAStepPresenter(EditQAStepView view, EmfDataset dataset, EditableQATabView tabView, EmfSession session) {
        this.view = view;
        this.tabView = tabView;
        this.dataset = dataset;
        this.session = session;
    }

    public void display(QAStep step) {
        view.observe(this);
        view.display(step, dataset, session.user());
    }

    public void doClose() {
        view.close();
    }

    public void doEdit() {
        tabView.refresh();
        doClose();
    }

}
