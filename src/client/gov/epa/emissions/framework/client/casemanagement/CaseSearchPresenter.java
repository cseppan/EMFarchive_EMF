package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;

public class CaseSearchPresenter {

    private EmfSession session;

    private CaseSearchView view;
    
    private CaseCategory[] caseCategoriesToInclude;
    
    private static CaseCategory lastCaseCategory = null;
    
    private static String lastNameContains = null;
    
    private static Case[] lastCases = null;

    public CaseSearchPresenter(CaseSearchView view, EmfSession session,
            CaseCategory[] categoriesToInclude) {
        this(view, session);
        this.caseCategoriesToInclude = categoriesToInclude;
    }
    
    public CaseSearchPresenter(CaseSearchView view, EmfSession session) {
        this.session = session;
        this.view = view;
    }
    
    public void display(CaseCategory defaultCategory, boolean selectSingle) throws Exception {
        view.observe(this);

        //get data...
        CaseCategory[] caseCategories = new CaseCategory[] {};
        if (caseCategoriesToInclude == null)
            caseCategories = session.caseService().getCaseCategories(); 
        else
            caseCategories = caseCategoriesToInclude;

        view.display(caseCategories, defaultCategory, selectSingle);
    }

    public void refreshCases(CaseCategory caseCategory, String nameContaining) throws EmfException {
        if ((lastCases!=null) && (lastCaseCategory!=null) && caseCategory.getName().equals(lastCaseCategory.getName()) && (nameContaining.equals(lastNameContains)))
        {
            // nothing has changed since last time, so just refresh with the previously retrieved list
            view.refreshCases(lastCases);
        }    
        else 
        {
            lastCases = session.caseService().getCases(caseCategory, nameContaining);
            view.refreshCases(lastCases);      
        }
        lastCaseCategory = caseCategory;
        lastNameContains = nameContaining;        
    }
    
    public Case[] getCases() {
        return view.getCases();
    }
    
    public EmfSession getSession(){
        return session; 
    }

}
