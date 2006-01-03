package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataCommonsService;

public class EditableSectorPresenter {

    private EditableSectorView editable;

    private Sector sector;

    private EmfSession session;

    private ViewableSectorView viewable;

    public EditableSectorPresenter(EmfSession session, EditableSectorView editable, ViewableSectorView viewable,
            Sector sector) {
        this.session = session;
        this.editable = editable;
        this.viewable = viewable;
        this.sector = sector;
    }

    public void doDisplay() throws EmfException {
        sector = service().getSectorLock(session.user(), sector);

        if (!sector.isLocked(session.user())) {// view mode, locked by another user
            new ViewableSectorPresenter(viewable, sector).doDisplay();
            return;
        }

        editable.observe(this);
        editable.display(sector);
    }

    private DataCommonsService service() {
        return session.dataCommonsService();
    }

    public void doClose() throws EmfException {
        service().releaseSectorLock(session.user(), sector);
        closeView();
    }

    private void closeView() {
        editable.close();
    }

    public void doSave(SectorsManagerView sectorManager) throws EmfException {
        sector = service().updateSector(session.user(), sector);
        
        sectorManager.refresh();
        closeView();
    }

}
