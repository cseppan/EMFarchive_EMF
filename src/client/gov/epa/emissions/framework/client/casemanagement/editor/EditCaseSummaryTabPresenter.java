package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;

public class EditCaseSummaryTabPresenter {

    private EmfSession session;

    public EditCaseSummaryTabPresenter(EmfSession session) {
        this.session = session;
    }

    public Sector[] getAllSectors() throws EmfException {
        return session.dataCommonsService().getSectors();
    }

}
