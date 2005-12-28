package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.ui.ViewLayout;

public class SectorsManagerPresenter {

    private SectorsManagerView view;

    private DataCommonsService service;

    private ViewLayout viewLayout;

    private EmfSession session;

    public SectorsManagerPresenter(EmfSession session, SectorsManagerView view, DataCommonsService service,
            ViewLayout viewLayout) {
        this.session = session;
        this.view = view;
        this.service = service;
        this.viewLayout = viewLayout;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display(service);
    }

    public void doClose() {
        view.close();
    }

    public void doUpdate(Sector sector, UpdateSectorView updateSectorView) throws EmfException {
        if (viewLayout.activate(sector.getName()))
            return;

        viewLayout.add(updateSectorView, sector.getName());
        UpdateSectorPresenter p = new UpdateSectorPresenter(session, updateSectorView, sector, service);
        p.doDisplay();
    }

}
