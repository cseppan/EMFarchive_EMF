package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;

public class ViewQAStepPresenter {

    QAStepView view;
    
    public ViewQAStepPresenter(QAStepView view) {
        this.view = view;
    }
    
    public void display(QAStep step, QAStepResult qaStepResult) {
        view.display(step, qaStepResult);
    }

}
