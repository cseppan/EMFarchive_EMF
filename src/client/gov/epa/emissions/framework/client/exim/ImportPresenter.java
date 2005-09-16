package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;

public class ImportPresenter {

    private ImportView view;

    private ExImServices model;

    private User user;

    public ImportPresenter(User user, ExImServices model) {
        this.user = user;
        this.model = model;
    }

    public void notifyImport(String directory, String filename, String datasetName, DatasetType type)
            throws EmfException {
        if (datasetName.length() == 0)
            throw new UserException("Dataset Name should be specified");
        if (directory.length() == 0)
            throw new UserException("Folder should be specified");

        if (filename.length() == 0)
            throw new UserException("Filename should be specified");

        EmfDataset dataset = new EmfDataset();
        dataset.setCreator(user.getFullName());
        dataset.setName(datasetName);

        // String filepath = directory + File.separator + filename;
        model.startImport(user, directory, filename, dataset, type);
    }

    public void notifyDone() {
        view.close();
    }

    public void observe(ImportView view) throws EmfException {
        this.view = view;
        
        view.register(this);
        view.setDefaultBaseFolder(model.getImportBaseFolder());
        view.display();
    }

    public void notifyBeginInput() {
        view.clearMessagePanel();
    }

}
