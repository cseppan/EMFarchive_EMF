package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;

public class PrintCasePresenter {
    private Case currentCase;

    private EmfSession session;

    private static String lastFolder = null;

    public PrintCasePresenter(EmfSession session, Case caseObj) {
        this.currentCase = caseObj;
        this.session = session;
    }

    public void display(PrintCaseDialog view) {
        view.observe(this);
        view.setMostRecentUsedFolder(getFolder());

        view.display();
    }

    private String getFolder() {
        return (lastFolder != null) ? lastFolder : getDefaultFolder();
    }

    public void printCase(String folder) throws EmfException {
        session.caseService().printCase(folder, currentCase.getId());
    }

    private String getDefaultFolder() {
        return currentCase.getOutputFileDir();
    }

}
