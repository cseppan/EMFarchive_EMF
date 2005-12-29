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

    public void doEdit(Sector sector, EditSectorView editSectorView, DisplaySectorView displaySectorView)
            throws EmfException {
        if (viewLayout.activate("Edit " + sector.getName()))
            return;

        viewLayout.add(editSectorView, "Edit " + sector.getName());
        EditSectorPresenter p = new EditSectorPresenter(session, editSectorView, displaySectorView, sector, service);
        p.doDisplay();
    }

    public void doView(Sector sector, DisplaySectorView displaySectorView) {
        if (viewLayout.activate(sector.getName()))
            return;

        viewLayout.add(displaySectorView, sector.getName());
        DisplaySectorPresenter p = new DisplaySectorPresenter(displaySectorView, sector);
        p.doDisplay();

    }

}
