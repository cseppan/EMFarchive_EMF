package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenter;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.editor.CaseEditorView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;

import java.util.Date;

public class SensitivityPresenter {
    private SensitivityView view;

    private EmfSession session;

    private CaseManagerPresenter managerPresenter;

    public SensitivityPresenter(EmfSession session, SensitivityView view, CaseManagerPresenter managerPresenter) {
        this.session = session;
        this.view = view;
        this.managerPresenter = managerPresenter;
    }

    public void doDisplay(Case case1) {
        view.observe(this);
        view.display(case1);
    }

    public void doClose() {
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public void doSave(Case newCase) throws EmfException {
        if (isDuplicate(newCase))
            throw new EmfException("A Case named '" + newCase.getName() + "' already exists.");

        newCase.setCreator(session.user());
        newCase.setLastModifiedBy(session.user());
        newCase.setLastModifiedDate(new Date());
        
        Case loaded = service().addCase(session.user(), newCase);
        closeView();
        managerPresenter.addNewCaseToTableData(loaded);
    }

    private boolean isDuplicate(Case newCase) throws EmfException {
        Case[] cases = service().getCases();
        for (int i = 0; i < cases.length; i++) {
            if (cases[i].getName().equals(newCase.getName()))
                return true;
        }

        return false;
    }

    private CaseService service() {
        return session.caseService();
    }

    public Case copyCase(int caseId) throws EmfException {
        return service().copyCaseObject(new int[] {caseId}, session.user())[0];
    }
    
    public Case updateCase(Case caseObj) throws EmfException {
        Case locked = service().obtainLocked(session.user(), caseObj);
        return service().updateCase(locked);
    }
    
    public void editCase(CaseEditorView caseView, Case caseObj) throws EmfException {
        CaseEditorPresenter presenter = new CaseEditorPresenterImpl(caseObj, session, caseView, managerPresenter);
        presenter.doDisplay();
    }

    public EmfSession getSession() {
        return this.session;
    }
}
