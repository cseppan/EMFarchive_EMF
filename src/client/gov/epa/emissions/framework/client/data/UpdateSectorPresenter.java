package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataService;

public class UpdateSectorPresenter {

    private UpdateSectorView view;

    private Sector sector;

    private DataService services;

    public UpdateSectorPresenter(UpdateSectorView view, Sector sector, DataService services) {
        this.view = view;
        this.sector = sector;
        this.services = services;
    }

    public void doDisplay() {
        view.observe(this);
        view.display(sector);
    }

    public void doClose() {
        view.close();
    }

    public void doSave(SectorsManagerView sectorManager) throws EmfException {
        services.updateSector(sector);
        sectorManager.refresh();
        doClose();
    }

}
