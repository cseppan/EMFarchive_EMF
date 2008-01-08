package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.CaseManagerPresenter;
import gov.epa.emissions.framework.client.casemanagement.history.ShowHistoryTabPresenter;
import gov.epa.emissions.framework.client.casemanagement.history.ShowHistoryTabView;
import gov.epa.emissions.framework.client.casemanagement.inputs.EditInputsTabPresenter;
import gov.epa.emissions.framework.client.casemanagement.inputs.EditInputsTabPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.inputs.EditInputsTabView;
import gov.epa.emissions.framework.client.casemanagement.jobs.EditJobsTabPresenter;
import gov.epa.emissions.framework.client.casemanagement.jobs.EditJobsTabPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.jobs.EditJobsTabView;
import gov.epa.emissions.framework.client.casemanagement.outputs.EditOutputsTabPresenter;
import gov.epa.emissions.framework.client.casemanagement.outputs.EditOutputsTabPresenterImpl;
import gov.epa.emissions.framework.client.casemanagement.outputs.EditOutputsTabView;
import gov.epa.emissions.framework.client.casemanagement.parameters.EditCaseParametersTabView;
import gov.epa.emissions.framework.client.casemanagement.parameters.EditParametersTabPresenter;
import gov.epa.emissions.framework.client.casemanagement.parameters.EditParametersTabPresenterImpl;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class CaseEditorPresenterImpl implements CaseEditorPresenter {
    private CaseEditorView view;

    private EmfSession session;

    private CaseManagerPresenter managerPresenter;

    private Case caseObj;

    private List presenters;

    private EditInputsTabPresenter inputPresenter;

    private EditJobsTabPresenter jobsPresenter;

    private EditParametersTabPresenter parametersPresenter;

    private EditOutputsTabPresenter outputPresenter;

    private EditableCaseSummaryTabPresenter summaryPresenter;

    private ShowHistoryTabPresenter historyPresenter;

    private boolean inputsLoaded = false;

    private boolean jobsLoaded = false;

    private boolean parameterLoaded = false;

    private boolean historyLoaded = false;

    private boolean outputsLoaded = false;

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
//        closeView();
    }

    void updateCase() throws EmfException {
        saveTabs();

        if (isDuplicate(caseObj))
            throw new EmfException("Duplicate name - '" + caseObj.getName() + "'.");

        caseObj.setLastModifiedBy(session.user());
        caseObj.setLastModifiedDate(new Date());
//        service().updateCase(caseObj);
        caseObj=service().updateCaseWithLock(caseObj);
//        managerPresenter.refreshWithLastCategory();
     
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
        summaryPresenter = new EditableCaseSummaryTabPresenterImpl(caseObj, summaryView);
        presenters.add(summaryPresenter);
    }

    public void set(EditInputsTabView inputsView) {
        inputPresenter = new EditInputsTabPresenterImpl(session, inputsView, caseObj);
        presenters.add(inputPresenter);
    }

    public void set(EditJobsTabView jobsView) {
        jobsPresenter = new EditJobsTabPresenterImpl(session, jobsView, caseObj);
        presenters.add(jobsPresenter);
    }

    public void set(EditOutputsTabView OutputsView) {
        outputPresenter = new EditOutputsTabPresenterImpl(session, OutputsView, caseObj);
        presenters.add(outputPresenter);
    }

    public void doExport(User user, String dirName, String purpose, boolean overWrite, Case caseToExport)
            throws EmfException {
        service().export(user, mapToRemote(dirName), purpose, overWrite, caseToExport.getId());
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

    public void set(EditCaseParametersTabView parameterview) {
        parametersPresenter = new EditParametersTabPresenterImpl(session, parameterview, caseObj);
        presenters.add(parametersPresenter);
    }

    public void set(ShowHistoryTabView caseHistoryView) {
        historyPresenter = new ShowHistoryTabPresenter(session, caseHistoryView, caseObj);
//        presenters.add(parametersPresenter);
    }

    public void doLoad(String tabTitle) throws EmfException {
        if (!inputsLoaded && tabTitle.equalsIgnoreCase("Inputs")) {
            inputPresenter.display();
            inputsLoaded = true;
        }

        if (!jobsLoaded && tabTitle.equalsIgnoreCase("Jobs")) {
            jobsPresenter.display();
            jobsLoaded = true;
        }

        if (!parameterLoaded && tabTitle.equalsIgnoreCase("Parameters")) {
            parametersPresenter.display();
            parameterLoaded = true;
        }

        if (!historyLoaded && tabTitle.equalsIgnoreCase("History")) {
            historyPresenter.display();
            historyLoaded = true;
        }

        if (!outputsLoaded && tabTitle.equalsIgnoreCase("Outputs")) {
            outputPresenter.display();
            outputsLoaded = true;
        }
    }
}
