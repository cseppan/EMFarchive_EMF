package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.EmfSession;
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
        CaseService caseService = session.caseService();
        return caseService;
    }

    public void doRemove(Case caseObj) throws EmfException {
        service().removeCase(caseObj);
    }

    public void doRefresh() throws EmfException {
        view.refresh(service().getCases());
    }

    public void doClose() {
        view.close();
    }

    public void doNew(NewCaseView view) {
        NewCasePresenter presenter = new NewCasePresenter(session, view, this);
        presenter.doDisplay();
    }
}
