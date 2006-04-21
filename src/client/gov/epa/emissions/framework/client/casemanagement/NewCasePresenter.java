package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseService;

public class NewCasePresenter {
    private NewCaseView view;

    private EmfSession session;

    public NewCasePresenter(EmfSession session, NewCaseView view) {
        this.session = session;
        this.view = view;
    }

    public void doDisplay() {
        view.observe(this);
        view.display();
    }

    public void doClose() {
        closeView();
    }

    private void closeView() {
        view.close();
    }

    public void doSave(Case newCase) throws EmfException {
        service().addCase(newCase);
        closeView();
    }

    private CaseService service() {
        return session.caseService();
    }

}
