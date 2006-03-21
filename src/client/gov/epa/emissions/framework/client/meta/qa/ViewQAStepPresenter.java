package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.QAStep;

public class ViewQAStepPresenter {

    ViewQAStepView view;
    
    public ViewQAStepPresenter(ViewQAStepView view) {
        this.view = view;
    }
    
    public void display(QAStep step) {
        view.display(step);
    }

}
