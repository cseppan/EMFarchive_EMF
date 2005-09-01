package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;

public class ExportPresenter {

    private User user;

    private ExImServices model;

    private ExportView view;

    public ExportPresenter(User user, ExImServices services, ExportView view) {
        this.user = user;
        this.model = services;
        this.view = view;
    }

    public void notifyExport(EmfDataset dataset, String filename) throws EmfException {
        model.startExport(user, dataset, filename);
    }

    public void notifyDone() {
        view.close();
    }

    public void observe() {
        view.register(this);
    }

}
