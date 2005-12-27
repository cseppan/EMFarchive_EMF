package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataCommonsService;

public class UpdateSectorPresenter {

    private UpdateSectorView view;

    private Sector sector;

    private DataCommonsService service;

    public UpdateSectorPresenter(UpdateSectorView view, Sector sector, DataCommonsService service) {
        this.view = view;
        this.sector = sector;
        this.service = service;
    }

    public void doDisplay() {
        view.observe(this);
        view.display(sector);
    }

    public void doClose() {
        view.close();
    }

    public void doSave(SectorsManagerView sectorManager) throws EmfException {
        service.updateSector(sector);
        sectorManager.refresh();
        doClose();
    }

}
