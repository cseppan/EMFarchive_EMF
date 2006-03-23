package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.data.QAStep;

public interface EditableQATabView {

    void display(QAStep[] steps, Version[] versions);

    void observe(EditableQAStepsPresenter presenter);

    void add(QAStep[] steps);

    QAStep[] steps();

}
