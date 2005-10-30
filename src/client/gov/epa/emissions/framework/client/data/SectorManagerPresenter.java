package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataServices;

public class SectorManagerPresenter {

    private SectorManagerView view;

    private DataServices services;

    public SectorManagerPresenter(SectorManagerView view, DataServices services) {
        this.view = view;
        this.services = services;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display(services);
    }

    public void doClose() {
        view.close();
    }

    public void doUpdateSector(Sector sector, UpdateSectorView updateSectorView) {
        UpdateSectorPresenter p = new UpdateSectorPresenter(updateSectorView, sector, services);
        p.doDisplay();
    }

}
