package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.EmfDataset;
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

    public void notifyImport(String filename, String datasetName, DatasetType type) throws EmfException {
        EmfDataset dataset = new EmfDataset();
        dataset.setCreator(user.getFullName());
        dataset.setName(datasetName);
        
        model.startImport(user, filename, dataset, type);
    }

    public void notifyDone() {
        view.close();
    }

    public void observe() {
        view.register(this);
    }

}
