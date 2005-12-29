package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataCommonsService;

public class EditSectorPresenter {

    private EditSectorView view;

    private Sector sector;

    private DataCommonsService service;

    private EmfSession session;

    public EditSectorPresenter(EmfSession session, EditSectorView view, Sector sector, DataCommonsService service) {
        this.session = session;
        this.view = view;
        this.sector = sector;
        this.service = service;
    }

    public void doDisplay() throws EmfException {
        sector = service.getSectorLock(session.user(), sector);
        view.observe(this);
        view.display(sector);
    }

    public void doClose() throws EmfException {
        service.releaseSectorLock(session.user(), sector);
        view.close();
    }

    public void doSave(SectorsManagerView sectorManager) throws EmfException {
        sector = service.updateSector(session.user(), sector);

        sectorManager.refresh();
        doClose();
    }

}
