package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;

public class ExportPresenter {

    private User user;

    private ExImServices model;

    private ExportView view;

    public ExportPresenter(User user, ExImServices services) {
        this.user = user;
        this.model = services;
    }

    public void notifyDone() {
        if (view != null)
            view.close();
    }

    public void observe(ExportView view) {
        this.view = view;
        view.register(this);
    }

    public void notifyExport(EmfDataset[] datasets, String directory) throws EmfException {
        model.startExport(user, datasets, directory);
    }

}
