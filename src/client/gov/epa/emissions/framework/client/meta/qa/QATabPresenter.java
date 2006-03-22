package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;

public interface QATabPresenter {

    public abstract void display() throws EmfException;

    public abstract void doView(QAStep step, QAStepView view);

}