package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public interface EditQAStepView extends ManagedView {

    void display(QAStep step, EmfDataset dataset, User user, String versionName);

    void observe(EditQAStepPresenter presenter);

}
