package gov.epa.emissions.framework.client.data;

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
        view.display(services.getSectors());
    }

    public void doClose() {
        view.close();
    }

}
