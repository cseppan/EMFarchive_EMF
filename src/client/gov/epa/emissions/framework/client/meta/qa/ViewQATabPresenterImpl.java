package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public class ViewQATabPresenterImpl implements ViewQATabPresenter {

    private QATabView view;

    private EmfSession session;

    private EmfDataset dataset;

    public ViewQATabPresenterImpl(QATabView view, EmfDataset dataset, EmfSession session) {
        this.view = view;
        this.dataset = dataset;
        this.session = session;
    }

    public void display() throws EmfException {
        view.observe(this);
        view.display(session.qaService().getQASteps(dataset), session);
    }

    public void doView(QAStep step, QAStepView view) throws EmfException {
        ViewQAStepPresenter presenter = new ViewQAStepPresenter(view, dataset, session);
        view(step, view, presenter);
    }

    void view(QAStep step, QAStepView view, ViewQAStepPresenter presenter) throws EmfException {
        view.observe(presenter);
        presenter.display(step, step.getVersion()+"");
    }

}
