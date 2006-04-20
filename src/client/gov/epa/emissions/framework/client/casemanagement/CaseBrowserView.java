package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.casemanagement.Case;

public interface CaseBrowserView extends ManagedView {

    void display(Case[] cases);

    void observe(CaseBrowserPresenter presenter);

    void refresh(Case[] cases);
}
