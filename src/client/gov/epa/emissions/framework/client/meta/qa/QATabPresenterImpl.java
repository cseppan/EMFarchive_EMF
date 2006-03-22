package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.qa.QAService;

public class QATabPresenterImpl implements QATabPresenter {

    private QATabView view;

    private QAService service;

    private EmfDataset dataset;

    public QATabPresenterImpl(QATabView view, EmfDataset dataset, QAService service) {
        this.view = view;
        this.dataset = dataset;
        this.service = service;
    }

    public void display() throws EmfException {
        view.observe(this);
        view.display(service.getQASteps(dataset));
    }

    public void doView(QAStep step, QAStepView view) {
        ViewQAStepPresenter presenter = new ViewQAStepPresenter(view);
        presenter.display(step);
    }

}
