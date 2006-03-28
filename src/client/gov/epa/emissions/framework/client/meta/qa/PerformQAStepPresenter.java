package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public class PerformQAStepPresenter {

    private PerformQAStepView view;
    private EmfDataset dataset;


    public PerformQAStepPresenter(PerformQAStepView view, EmfDataset dataset) {
        this.view = view;
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
        doClose();
    }

}
