package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.data.QAStep;

public interface PerformQAStepView extends ManagedView {

    void display(QAStep step);

    void observe(PerformQAStepPresenter presenter);

}
