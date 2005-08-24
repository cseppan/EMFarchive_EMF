package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;

public class ImportPresenter {

    private ImportView view;

    private ExImServices model;

    private User user;

    public ImportPresenter(User user, ExImServices model, ImportView view) {
        this.user = user;
        this.model = model;
        this.view = view;
    }

    public void notifyImport(DatasetType type, String filename) throws EmfException {
        model.startImport(user, filename, type);
    }

    public void notifyDone() {
        view.close();
    }

    public void observe() {
        view.register(this);
    }

}
