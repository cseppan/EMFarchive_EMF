package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
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

    public void display(QAStep step, String versionName) throws EmfException {
        view.observe(this);
        QAProgram[] programs = session.qaService().getQAPrograms();
        view.display(step, programs, dataset, session.user(), versionName);
    }

    public void doClose() {
        view.disposeView();
    }

    public void doSave() throws EmfException {
        view.save();
        tabView.refresh();
        doClose();
    }

    public void doRun() throws EmfException {
        QAStep step = view.save();
        tabView.refresh();
        session.qaService().runQAStep(step, session.user());
    }

}
