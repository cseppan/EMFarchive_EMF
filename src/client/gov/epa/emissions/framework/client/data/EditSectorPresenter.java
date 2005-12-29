package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataCommonsService;

public class EditSectorPresenter {

    private EditSectorView editView;

    private Sector sector;

    private DataCommonsService service;

    private EmfSession session;

    private DisplaySectorView displayView;

    public EditSectorPresenter(EmfSession session, EditSectorView editView, DisplaySectorView displayView,
            Sector sector, DataCommonsService service) {
        this.session = session;
        this.editView = editView;
        this.displayView = displayView;
        this.sector = sector;
        this.service = service;
    }

    public void doDisplay() throws EmfException {
        sector = service.getSectorLock(session.user(), sector);
        
        if (!sector.isLocked(session.user())) {// view mode, locked by another user
            new DisplaySectorPresenter(displayView, sector).doDisplay();
            return;
        }

        editView.observe(this);
        editView.display(sector);
    }

    public void doClose() throws EmfException {
        service.releaseSectorLock(session.user(), sector);
        editView.close();
    }

    public void doSave(SectorsManagerView sectorManager) throws EmfException {
        sector = service.updateSector(session.user(), sector);

        sectorManager.refresh();
        doClose();
    }

}
