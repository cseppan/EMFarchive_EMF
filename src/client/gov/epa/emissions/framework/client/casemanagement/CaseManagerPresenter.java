package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;

public interface CaseManagerPresenter {

    void display() throws EmfException;

    void doRemove(Case caseObj) throws EmfException;

    void doRefresh() throws EmfException;
    
    void doRefresh(CaseCategory category) throws EmfException;

    void doClose();

    void doNew(NewCaseView view);
    
    void doSaveCopiedCase(Case newCase, String templateused) throws EmfException;
    
    void addNewCaseToTableData(Case newCase);

    void refreshWithLastCategory() throws EmfException;

}