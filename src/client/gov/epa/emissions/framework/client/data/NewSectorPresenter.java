package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataCommonsService;

public class NewSectorPresenter {
    private NewSectorView view;

    private Sector sector;

    private EmfSession session;

    public NewSectorPresenter(EmfSession session, Sector sector, NewSectorView view) {
        this.session = session;
        this.sector = sector;
        this.view = view;
    }
    
    public void doDisplay() {
        view.observe(this);
        view.display(sector);
    }

    private DataCommonsService service() {
        return session.dataCommonsService();
    }

    public void doClose() {
        view.close();
    }

    public void doSave(SectorsManagerView sectorManager) throws EmfException {
        service().addSector(sector);

        sectorManager.refresh();
        doClose();
    }
    
}
