package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;

public interface QAStepView {
    void display(QAStep step, QAStepResult qaStepResult);
}
