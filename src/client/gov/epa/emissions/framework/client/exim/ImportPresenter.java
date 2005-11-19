package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImService;
import gov.epa.emissions.framework.services.User;

import java.util.Date;

public class ImportPresenter {

    private ImportView view;

    private ExImService eximServices;

    private User user;

    public ImportPresenter(User user, ExImService eximServices) {
        this.user = user;
        this.eximServices = eximServices;
    }

    public void doImport(String directory, String filename, String datasetName, DatasetType type) throws EmfException {
        if (datasetName.length() == 0)
            throw new UserException("Dataset Name should be specified");
        if (directory.length() == 0)
            throw new UserException("Folder should be specified");

        if (filename.length() == 0)
            throw new UserException("Filename should be specified");

        EmfDataset dataset = new EmfDataset();
        dataset.setCreator(user.getFullName());
        dataset.setDatasetType(type);
        dataset.setName(datasetName);
        dataset.setCreatedDateTime(new Date());
        dataset.setModifiedDateTime(dataset.getCreatedDateTime());
        dataset.setAccessedDateTime(dataset.getCreatedDateTime());

        eximServices.startImport(user, directory, filename, dataset);
    }

    public void doDone() {
        view.close();
    }

    public void display(ImportView view) throws EmfException {
        this.view = view;

        view.register(this);
        view.setDefaultBaseFolder(eximServices.getImportBaseFolder());

        view.display();
    }

    public void notifyBeginInput() {
        view.clearMessagePanel();
    }

}
