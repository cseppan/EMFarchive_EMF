package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.casemanagement.Case;

public interface CaseManagerView extends ManagedView {

    void display(Case[] cases);

    void observe(CaseManagerPresenterImpl presenter);

    void refresh(Case[] cases);
}
