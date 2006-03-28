package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.data.QAStep;

public interface EditableQATabView {

    void display(QAStep[] steps, Version[] versions);

    void observe(EditableQATabPresenter presenter);

    void add(QAStep[] steps);

    void add(QAStep step);

    QAStep[] steps();

    void setStatus(QAStep step);

    void informLackOfTemplatesForAddingNewSteps(DatasetType type);

}
