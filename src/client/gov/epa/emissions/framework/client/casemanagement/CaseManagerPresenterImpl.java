package gov.epa.emissions.framework.client.casemanagement;

import java.util.Date;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;
import gov.epa.emissions.framework.ui.RefreshObserver;

public class CaseManagerPresenterImpl implements RefreshObserver, CaseManagerPresenter {

    private CaseManagerView view;

    private EmfSession session;

    public CaseManagerPresenterImpl(EmfSession session, CaseManagerView view) {
        this.session = session;
        this.view = view;
    }

    public void display() throws EmfException {
        view.display(service().getCases());
        view.observe(this);
    }

    private CaseService service() {
        return session.caseService();
    }

    public void doRemove(Case caseObj) throws EmfException {
        service().removeCase(caseObj);
    }

    public void doRefresh() throws EmfException {
        view.refresh(service().getCases());
    }

    public void doClose() {
        view.disposeView();
    }

    public void doNew(NewCaseView view) {
        NewCasePresenter presenter = new NewCasePresenter(session, view, this);
        presenter.doDisplay();
    }

    public void doSaveCopiedCase(Case newCase, String templateused) throws EmfException {
        if (isDuplicate(newCase))
            throw new EmfException("A Case named '" + newCase.getName() + "' already exists.");

        newCase.setLastModifiedBy(session.user());
        newCase.setLastModifiedDate(new Date());
        newCase.setTemplateUsed(templateused);

        service().addCase(session.user(), newCase);
        doRefresh();
    }
    
    private boolean isDuplicate(Case newCase) throws EmfException {
        Case[] cases = service().getCases();
        for (int i = 0; i < cases.length; i++) {
            if (cases[i].getName().equals(newCase.getName()))
                return true;
        }

        return false;
    }

    public void doEdit(CaseEditorView caseView, Case caseObj) throws EmfException {
        CaseEditorPresenter presenter = new CaseEditorPresenterImpl(caseObj, session, caseView, this);
        displayEditor(presenter);
    }

    void displayEditor(CaseEditorPresenter presenter) throws EmfException {
        presenter.doDisplay();
    }

    public void doCopyCases(int[] caseIds) throws EmfException {
        service().copyCaseObject(caseIds, session.user());
    }
}
