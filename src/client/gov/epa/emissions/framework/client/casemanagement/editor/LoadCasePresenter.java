package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;

public class LoadCasePresenter {
    private Case currentCase;

    private EmfSession session;

    private static String lastFolder = null;

    public LoadCasePresenter(EmfSession session, Case caseObj) {
        this.currentCase = caseObj;
        this.session = session;
    }

    public void display(LoadCaseDialog view) {
        view.observe(this);
        view.setMostRecentUsedFolder(getFolder());

        view.display();
    }

    private String getFolder() {
        return (lastFolder != null) ? lastFolder : getDefaultFolder();
    }

    public void printCase(String path) throws EmfException {
        session.caseService().loadCMAQCase(path, currentCase.getId(), session.user());
    }

    private String getDefaultFolder() {
        return "";
    }

}
