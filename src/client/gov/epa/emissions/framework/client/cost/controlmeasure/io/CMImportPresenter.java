package gov.epa.emissions.framework.client.cost.controlmeasure.io;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;

import java.io.File;

public class CMImportPresenter {

    private CMImportView view;

    private EmfSession session;

    private CMImportInputRules importRules;

    public CMImportPresenter(EmfSession session) {
        this.session = session;
        this.importRules = new CMImportInputRules();
    }

    public void doImport(String directory, String[] files) throws EmfException {
        importControlMeasures(directory, files);
    }

    void importControlMeasures(String directory, String[] files) throws EmfException {
        importRules.validate(directory, files);
        startImportMessage(view);
        session.controlMeasureService().importControlMeasures(mapToRemote(directory), files, session.user());
    }

    private void startImportMessage(CMImportView view) {
        String message = "Started Import. Please monitor the Status window to track your Import request.";
        view.setMessage(message);
    }

    public void doDone() {
        view.disposeView();
    }

    public void display(CMImportView view) {
        this.view = view;
        view.register(this);
        view.setDefaultBaseFolder(getDefaultBaseFolder());

        view.display();
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

    // TODO: move the getFileNamesFromPattern () to a common service
    public String[] getFilesFromPatten(String folder, String pattern) throws EmfException {
        return session.eximService().getFilenamesFromPattern(mapToRemote(folder), pattern);
    }

}
