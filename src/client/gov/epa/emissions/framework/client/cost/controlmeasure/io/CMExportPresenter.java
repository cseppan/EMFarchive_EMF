package gov.epa.emissions.framework.client.cost.controlmeasure.io;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.cost.ControlMeasure;

import java.io.File;

public class CMExportPresenter {

    private CMExportView view;

    private EmfSession session;

    private static String lastFolder = null;

    public CMExportPresenter(EmfSession session) {
        this.session = session;
    }

    public void notifyDone() {
        view.disposeView();
    }

    public void display(CMExportView view) {
        this.view = view;
        view.observe(this);
        view.setMostRecentUsedFolder(getFolder());

        view.display();
    }

    private String getFolder() {
        return (lastFolder != null) ? lastFolder : getDefaultFolder();
    }

    public void doExportWithOverwrite(ControlMeasure[] controlMeasures, String folder, String prefix) {
        doExport(controlMeasures, folder, true, prefix);
    }

    public void doExport(ControlMeasure[] controlMeasures, String folder, String prefix) {
        doExport(controlMeasures, folder, false, prefix);
    }

    private void doExport(ControlMeasure[] datasets, String folder, boolean overwrite, String prefix) {
        //ExImService services = session.eximService();
        
        session.setMostRecentExportFolder(folder);

        File dir = new File(folder);
        if (dir.isDirectory())
            lastFolder = folder;

//        if (overwrite)
            //services.exportDatasetsWithOverwrite(session.user(), datasets, versions, mapToRemote(folder), purpose);
//        else
            //services.exportDatasets(session.user(), datasets, versions, mapToRemote(folder), purpose);
    }

//    private String mapToRemote(String dir) {
//        return session.preferences().mapLocalOutputPathToRemote(dir);
//    }

    private String getDefaultFolder() {
        String folder = session.preferences().outputFolder();
        if (!new File(folder).isDirectory())
            folder = "";// default, if unspecified

        return folder;
    }
}
