package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorView;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseViewerPresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseViewerPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseViewerView;
import gov.epa.emissions.framework.client.casemanagement.sensitivity.SensitivityPresenter;
import gov.epa.emissions.framework.client.casemanagement.sensitivity.SensitivityView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseCategory;
import gov.epa.emissions.framework.services.casemanagement.CaseService;

public class CaseManagerPresenterImpl implements CaseManagerPresenter {

    private CaseManagerView view;

    private EmfSession session;
    
    private CaseObjectManager caseObjectManager = null;

    public CaseManagerPresenterImpl(EmfSession session, CaseManagerView view) {
        this.session = session;
        this.view = view;
        this.caseObjectManager = CaseObjectManager.getCaseObjectManager(session);
    }

    public void display() {
        view.observe(this);
        view.display();
    }

    private CaseService service() {
        return session.caseService();
    }

    public void doRemove(Case caseObj) throws EmfException {
        service().removeCase(caseObj);
    }

    public void doRefresh() throws EmfException{
        //view.refresh(service().getCases());
        view.refreshWithLastCategory();
    }

    public void doClose() {
        view.disposeView();
    }

    public void doNew(NewCaseView view) {
        NewCasePresenter presenter = new NewCasePresenter(session, view, this);
        presenter.doDisplay();
    }
    
    public void doSensitivity(SensitivityView view, Case case1) {
        SensitivityPresenter presenter = new SensitivityPresenter(session, view, this);
        presenter.doDisplay(case1, this);
    }
    
    public void addNewCaseToTableData(Case newCase) {
        view.addNewCaseToTableData(newCase);
    }

//    public void doSaveCopiedCase(Case newCase, String templateused) throws EmfException {
//        if (isDuplicate(newCase))
//            throw new EmfException("A Case named '" + newCase.getName() + "' already exists.");
//
//        newCase.setLastModifiedBy(session.user());
//        newCase.setLastModifiedDate(new Date());
//        newCase.setTemplateUsed(templateused);
//
//        service().addCase(session.user(), newCase);
//        //doRefresh();
//    }
//    
//    private boolean isDuplicate(Case newCase) throws EmfException {
//        Case[] cases = service().getCases();
//        for (int i = 0; i < cases.length; i++) {
//            if (cases[i].getName().equals(newCase.getName()))
//                return true;
//        }
//
//        return false;
//    }

    public void doEdit(CaseEditorView caseView, Case caseObj) throws EmfException {
        CaseEditorPresenter presenter = new CaseEditorPresenterImpl(caseObj, session, caseView, this);
        displayEditor(presenter);
    }
    
    public void doView(CaseViewerView caseView, Case caseObj) throws EmfException {
        CaseViewerPresenter presenter = new CaseViewerPresenterImpl(caseObj, session, caseView, this);
        presenter.doDisplay();
    }

    void displayEditor(CaseEditorPresenter presenter) throws EmfException {
        presenter.doDisplay();
    }

    public void doCopyCases(int[] caseIds) throws EmfException {
        startCopyMessage();
        service().copyCaseObject(caseIds, session.user());
    }
    
    private void startCopyMessage() {
        String message = "Started copy. Please monitor the Status window to track your Copy request.";
        view.setMessage(message);
    }

    public CaseCategory[] getCategories() throws EmfException {
        return service().getCaseCategories();
    }

    public Case[] getCases(CaseCategory category ) throws EmfException {
        this.caseObjectManager.refresh();
        this.caseObjectManager.refreshJobList();
        
        if (category == null)
            return new Case[0];
        
        if (category.getName().equals("All"))
            return service().getCases();
        
        return service().getCases(category);
    }
    
    public Case[] getCases(CaseCategory category, String nameContains) throws EmfException {
        this.caseObjectManager.refresh();
        this.caseObjectManager.refreshJobList();
        
        if (category == null){
            view.setSelectedCategory();
            return service().getCases(nameContains);
        }
        if (category.getName().equals("All"))
            return service().getCases(nameContains);
        
        return service().getCases(category, nameContains);
        //eturn cases==null?  new Case[0] :cases;
    }

    public void refreshWithLastCategory() throws EmfException {
        view.refreshWithLastCategory();
    }

    public String checkParentCase(Case caseObj) throws EmfException {
        return service().checkParentCase(caseObj);
    }

    public CaseCategory getSelectedCategory() {
        return view.getSelectedCategory();
    }
}
