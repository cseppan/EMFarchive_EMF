package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataCommonsService;

public class SectorsManagerPresenter {

    private SectorsManagerView view;

    private DataCommonsService service;

    private EmfSession session;

    public SectorsManagerPresenter(EmfSession session, SectorsManagerView view, DataCommonsService service) {
        this.session = session;
        this.view = view;
        this.service = service;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display(service);
    }

    public void doClose() {
        view.close();
    }

    public void doEdit(Sector sector, EditableSectorView editSectorView, ViewableSectorView displaySectorView)
            throws EmfException {
        EditableSectorPresenter p = new EditableSectorPresenterImpl(session, editSectorView, displaySectorView, sector);
        edit(p);
    }
    
    public void displayNewSector(Sector sector, EditableSectorView editSectorView, ViewableSectorView displaySectorView) {
        EditableSectorPresenter p = new EditableSectorPresenterImpl(session, editSectorView, displaySectorView, sector);
        p.displayNewSector();
    }

    void edit(EditableSectorPresenter presenter) throws EmfException {
        presenter.doDisplay();
    }

    public void doView(Sector sector, ViewableSectorView viewable) {
        ViewableSectorPresenter p = new ViewableSectorPresenterImpl(viewable, sector);
        view(p);
    }

    void view(ViewableSectorPresenter presenter) {
        presenter.doDisplay();
    }

}
