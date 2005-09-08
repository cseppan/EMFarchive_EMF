package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;

public class ExportPresenter {

    private User user;

    private ExImServices model;

    private ExportView view;

    public ExportPresenter(EmfSession session) {
        this.user = session.getUser();
        this.model = session.getExImServices();
    }

    public void notifyDone() {
        if (view != null)
            view.close();
    }

    public void observe(ExportView view) {
        this.view = view;
        view.register(this);
    }

    public void notifyExport(EmfDataset[] datasets, String folder) throws EmfException {
        model.startExport(user, datasets, folder);
    }

}
