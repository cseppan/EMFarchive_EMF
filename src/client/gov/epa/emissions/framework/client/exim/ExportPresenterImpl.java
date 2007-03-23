package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ExImService;

import java.io.File;
import java.util.Date;

public class ExportPresenterImpl implements ExportPresenter {

    private ExportView view;

    private EmfSession session;

    private static String lastFolder = null;

    public ExportPresenterImpl(EmfSession session) {
        this.session = session;
    }

    public void notifyDone() {
        view.disposeView();
    }

    public void display(ExportView view) {
        this.view = view;
        view.observe(this);
        view.setMostRecentUsedFolder(getFolder());

        view.display();
    }

    private String getFolder() {
        return (lastFolder != null) ? lastFolder : getDefaultFolder();
    }

    public void doExportWithOverwrite(EmfDataset[] datasets, String folder, String purpose) throws EmfException {
        doExport(datasets, folder, true, purpose);
    }

    public void doExport(EmfDataset[] datasets, String folder, String purpose) throws EmfException {
        doExport(datasets, folder, false, purpose);
    }

    private void doExport(EmfDataset[] datasets, String folder, boolean overwrite, String purpose) throws EmfException {
        ExImService services = session.eximService();
        Version[] versions = new Version[datasets.length];
        
        for (int i = 0; i < datasets.length; i++) {
            datasets[i].setAccessedDateTime(new Date());
            versions[i] = services.getVersion(datasets[i], datasets[i].getDefaultVersion());
        }
        
        session.setMostRecentExportFolder(folder);

        File dir = new File(folder);
        if (dir.isDirectory())
            lastFolder = folder;

        if (overwrite)
            services.exportDatasetsWithOverwrite(session.user(), datasets, versions, folder, purpose);
        else
            services.exportDatasets(session.user(), datasets, versions, folder, purpose);
    }

//    private String mapToRemote(String dir) {
//        return session.preferences().mapLocalOutputPathToRemote(dir);
//    }

    private String getDefaultFolder() {
        String folder = session.preferences().outputFolder();
        //TODO: really need to check on the server side
//        if (!new File(folder).isDirectory())
//            folder = "";// default, if unspecified

        return folder;
    }
}
