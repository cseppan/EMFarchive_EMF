package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;

public interface EditableQATabView {

    void observe(EditableQAStepsPresenter presenter);

    void save() throws EmfException;

    void add(QAStep[] steps);

    void display(QAStep[] steps);

}
