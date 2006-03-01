package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.ExImService;

import java.io.File;

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

    public void doImport(String directory, String[] filePattern, DatasetType type)
            throws EmfException {
        service.importDatasets(user, mapToRemote(directory), filePattern, type);
    }
    
    public void doImport(String directory, String[] filePattern, DatasetType type, String datasetName)
            throws EmfException {
        service.importDataset(user, mapToRemote(directory), filePattern, type, datasetName);
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
