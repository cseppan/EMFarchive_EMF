package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.MessagePanel;

public class EditCaseSummaryTabPresenter {

    private MessagePanel messagePanel;

    private EmfSession session;

    public EditCaseSummaryTabPresenter(EditableCaseSummaryTab view, MessagePanel messagePanel, EmfSession session) {
        this.messagePanel = messagePanel;
        this.session = session;
    }

    public Sector[] getAllSectors() {
        try {
            return session.dataCommonsService().getSectors();
        } catch (EmfException e) {
            messagePanel.setError("Could not get all the sectors");
        }
        return null;
    }

}
