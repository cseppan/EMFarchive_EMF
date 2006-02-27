package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImService;

import java.io.File;
import java.util.Date;

public class ImportPresenter {

    private ImportView view;

    private ExImService service;

    private User user;

    private EmfSession session;

    public ImportPresenter(EmfSession session, User user, ExImService service) {
        this.user = user;
        this.service = service;
        this.session = session;
    }

    public void doImportDatasetForEveryFileInPattern(String directory, String filePattern, DatasetType type)
            throws EmfException {
        if (filePattern.length() == 0)
            throw new EmfException("Filename should be specified");

        if (directory.length() == 0)
            throw new EmfException("Folder should be specified");

        if (filePattern.length() == 0)
            throw new EmfException("Filename should be specified");

        if (type.getName().equals("Choose a type ..."))
            throw new EmfException("Dataset Type should be selected");

        service.importDatasetForEveryFileInPattern(user, mapToRemote(directory), filePattern, type);
    }

    public void doImportDatasetForEachFile(String directory, String[] filenames, DatasetType type) throws EmfException {
        service.importDatasetForEachFile(user, mapToRemote(directory), filenames, type);
    }

    public void doImportDatasetUsingSingleFile(String directory, String filename, String datasetName, DatasetType type)
            throws EmfException {
        validate(directory, datasetName, type);
        if (filename.length() == 0)
            throw new EmfException("Filename should be specified");
        EmfDataset dataset = createDataset(datasetName, type);

        service.importDatasetUsingSingleFile(user, mapToRemote(directory), filename, dataset);
    }

    private void validate(String directory, String datasetName, DatasetType type) throws EmfException {
        if (datasetName.length() == 0)
            throw new EmfException("Dataset Name should be specified");
        if (directory.length() == 0)
            throw new EmfException("Folder should be specified");

        if (type.getName().equals("Choose a type ..."))
            throw new EmfException("Dataset Type should be selected");
    }

    public void doImportDatasetUsingMultipleFiles(String directory, String[] filenames, String datasetName,
            DatasetType type) throws EmfException {
        validate(directory, datasetName, type);
        EmfDataset dataset = createDataset(datasetName, type);

        service.importDatasetUsingMultipleFiles(user, mapToRemote(directory), filenames, dataset);
    }

    private EmfDataset createDataset(String datasetName, DatasetType type) {
        EmfDataset dataset = new EmfDataset();
        dataset.setCreator(user.getUsername());
        dataset.setDatasetType(type);
        dataset.setName(datasetName);
        dataset.setCreatedDateTime(new Date());
        dataset.setModifiedDateTime(dataset.getCreatedDateTime());
        dataset.setAccessedDateTime(dataset.getCreatedDateTime());

        return dataset;
    }

    public void doDone() {
        view.close();
    }

    public void display(ImportView view) {
        this.view = view;
        view.register(this);
        view.setDefaultBaseFolder(getDefaultBaseFolder());

        view.display();
    }

    public void notifyBeginInput() {
        view.clearMessagePanel();
    }

    private String getDefaultBaseFolder() {
        String folder = session.preferences().inputFolder();
        if (!new File(folder).isDirectory())
            folder = "";// default, if unspecified

        return folder;
    }

    private String mapToRemote(String dir) {
        return session.preferences().mapLocalInputPathToRemote(dir);
    }

}
