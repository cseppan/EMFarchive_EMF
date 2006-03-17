package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.QAStep;

public interface QATabView {

    void observe(QAStepsPresenter presenter);

    void save();

    void add(QAStep[] steps);

    void display(QAStep[] steps);

}
