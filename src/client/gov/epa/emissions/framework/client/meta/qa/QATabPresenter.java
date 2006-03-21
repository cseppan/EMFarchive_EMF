package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.qa.QAService;

public class QATabPresenter {

    private ViewableQATabView view;

    private QAService service;

    private EmfDataset dataset;

    public QATabPresenter(ViewableQATabView view, EmfDataset dataset, QAService service) {
        this.view = view;
        this.dataset = dataset;
        this.service = service;
    }

    public void display() throws EmfException {
        view.display(service.getQASteps(dataset));
    }

}
