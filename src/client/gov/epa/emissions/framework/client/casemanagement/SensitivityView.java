package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.casemanagement.Case;

public interface SensitivityView extends ManagedView {

    void observe(SensitivityPresenter presenter);

    void display(Case case1);

}