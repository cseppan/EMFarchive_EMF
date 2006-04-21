package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;

public class EditCasePresenter {
    private EditCaseView view;

    private EmfSession session;

    private CaseManagerPresenter managerPresenter;

    private Case caseObj;

    public EditCasePresenter(Case caseObj, EmfSession session, EditCaseView view, CaseManagerPresenter managerPresenter) {
        this.caseObj = caseObj;
        this.session = session;
        this.view = view;
        this.managerPresenter = managerPresenter;
    }

    public void doDisplay() {
        view.observe(this);
        view.display(caseObj);
    }

    public void doClose() {
        closeView();
    }

    private void closeView() {
        view.close();
    }

    public void doSave() throws EmfException {
        if (isDuplicate(caseObj))
            throw new EmfException("Duplicate name - '" + caseObj.getName() + "'.");

        service().updateCase(caseObj);
        closeView();
        managerPresenter.doRefresh();
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

}
