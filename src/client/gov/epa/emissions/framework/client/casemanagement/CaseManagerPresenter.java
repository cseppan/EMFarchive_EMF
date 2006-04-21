package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;

public interface CaseManagerPresenter {

    public abstract void display() throws EmfException;

    public abstract void doRemove(Case caseObj) throws EmfException;

    public abstract void doRefresh() throws EmfException;

    public abstract void doClose();

    public abstract void doNew(NewCaseView view);

}