package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.CaseManagerPresenter;
import gov.epa.emissions.framework.client.casemanagement.inputs.EditInputsTabPresenter;
import gov.epa.emissions.framework.client.casemanagement.inputs.EditInputsTabPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.inputs.EditInputsTabView;
import gov.epa.emissions.framework.client.casemanagement.outputs.EditOutputsTabPresenter;
import gov.epa.emissions.framework.client.casemanagement.outputs.EditOutputsTabPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.outputs.EditOutputsTabView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CaseEditorPresenterImpl implements CaseEditorPresenter {
    private CaseEditorView view;

    private EmfSession session;

    private CaseManagerPresenter managerPresenter;

    private Case caseObj;

    private List presenters;

    public CaseEditorPresenterImpl(Case caseObj, EmfSession session, CaseEditorView view,
            CaseManagerPresenter managerPresenter) {
        this.caseObj = caseObj;
        this.session = session;
        this.view = view;
        this.managerPresenter = managerPresenter;
        presenters = new ArrayList();
    }

    public void doDisplay() throws EmfException {
        view.observe(this);

        caseObj = service().obtainLocked(session.user(), caseObj);
        if (!caseObj.isLocked(session.user())) {// view mode, locked by another user
            view.notifyLockFailure(caseObj);
            return;
        }

        view.display(caseObj);
    }

    public void doClose() throws EmfException {
        service().releaseLocked(caseObj);
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public void doSave() throws EmfException {
        updateCase();
        closeView();
    }

    void updateCase() throws EmfException {
        if (isDuplicate(caseObj))
            throw new EmfException("Duplicate name - '" + caseObj.getName() + "'.");
        
        saveTabs();
        service().updateCase(caseObj);
        managerPresenter.doRefresh();
    }

    private void saveTabs() throws EmfException {
        for (Iterator iter = presenters.iterator(); iter.hasNext();) {
            CaseEditorTabPresenter element = (CaseEditorTabPresenter) iter.next();
            element.doSave();
        }
    }

    private boolean isDuplicate(Case newCase) throws EmfException {
        Case[] cases = service().getCases();
        for (int i = 0; i < cases.length; i++) {
            if (cases[i].getName().equals(newCase.getName()) && cases[i].getId() != newCase.getId())
                return true;
        }

        return false;
    }

    private CaseService service() {
        return session.caseService();
    }

    public void set(EditableCaseSummaryTabView summaryView) {
        EditableCaseSummaryTabPresenterImpl summaryPresenter = new EditableCaseSummaryTabPresenterImpl(caseObj,
                summaryView);
        presenters.add(summaryPresenter);
    }

    public void set(EditInputsTabView inputsView) throws EmfException {
        EditInputsTabPresenter inputPresenter = new EditInputsTabPresenterImpl(session, inputsView, caseObj);
        inputPresenter.display();
        
        presenters.add(inputPresenter);
    }

    public void set(EditOutputsTabView OutputsView) throws EmfException {
        EditOutputsTabPresenter outputPresenter = new EditOutputsTabPresenterImpl(session, OutputsView, caseObj);
        outputPresenter.display();
        
        presenters.add(outputPresenter);
    }

    public void doExport(User user, String dirName, String purpose, boolean overWrite, Case caseToExport) throws EmfException {
        service().export(user, mapToRemote(dirName), purpose, overWrite, caseToExport);
    }
    
    private String mapToRemote(String dir) {
        return session.preferences().mapLocalOutputPathToRemote(dir);
    }

    public void doSaveWithoutClose() throws EmfException {
        updateCase();
        managerPresenter.doRefresh();
        
        caseObj = service().obtainLocked(session.user(), caseObj); // get lock after release it
        if (!caseObj.isLocked(session.user())) {// view mode, locked by another user
            closeView();
            return;
        }
    }

    public void set(EditableCaseParameterTab parameterview) {
        EditableCaseParameterTabPresenterImpl parameterPresenter = new EditableCaseParameterTabPresenterImpl(caseObj,
                parameterview);
        presenters.add(parameterPresenter);
    }
}
