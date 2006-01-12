package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImService;

import java.util.Date;

public class ImportPresenter {

    private ImportView view;

    private ExImService service;

    private User user;

    public ImportPresenter(User user, ExImService service) {
        this.user = user;
        this.service = service;
    }

    public void doImport(String directory, String filename, String datasetName, DatasetType type) throws EmfException {
        if (datasetName.length() == 0)
            throw new EmfException("Dataset Name should be specified");
        if (directory.length() == 0)
            throw new EmfException("Folder should be specified");

        if (filename.length() == 0)
            throw new EmfException("Filename should be specified");

        if (type.getName().equals("Choose a type ..."))
            throw new EmfException("Dataset Type should be selected");

        EmfDataset dataset = new EmfDataset();
        dataset.setCreator(user.getFullName());
        dataset.setDatasetType(type);
        dataset.setName(datasetName);
        dataset.setCreatedDateTime(new Date());
        dataset.setModifiedDateTime(dataset.getCreatedDateTime());
        dataset.setAccessedDateTime(dataset.getCreatedDateTime());

        service.startImport(user, directory, filename, dataset);
    }

    public void doDone() {
        view.close();
    }

    public void display(ImportView view) throws EmfException {
        this.view = view;

        view.register(this);
        view.setDefaultBaseFolder(getDefaultBaseFolderForImport());

        view.display();
    }

    public void notifyBeginInput() {
        view.clearMessagePanel();
    }

    private String getDefaultBaseFolderForImport() throws EmfException {
        return service.getImportBaseFolder();
    }

}
