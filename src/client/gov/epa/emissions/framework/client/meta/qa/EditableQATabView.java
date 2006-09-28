package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.data.QAStep;

public interface EditableQATabView {

    void display(Dataset dataset, QAStep[] steps, Version[] versions);

    void observe(EditableQATabPresenter presenter);

    void addFromTemplate(QAStep[] steps);

    void addCustomQAStep(QAStep step);

    QAStep[] steps();

    void informLackOfTemplatesForAddingNewSteps(DatasetType type);

    void refresh();

}
