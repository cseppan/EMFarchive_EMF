package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.QAStep;

public class PerformQAStepPresenter {

    private PerformQAStepView view;

    public PerformQAStepPresenter(PerformQAStepView view) {
        this.view = view;
    }

    public void display(QAStep step) {
        view.observe(this);
        view.display(step);
    }

    public void doClose() {
        view.close();
    }

}
