package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.QAStep;

public interface QATabView {
    void display(QAStep[] steps);

    void observe(QATabPresenter presenter);
}
