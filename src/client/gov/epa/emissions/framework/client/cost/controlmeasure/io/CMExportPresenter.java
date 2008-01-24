package gov.epa.emissions.framework.client.cost.controlmeasure.io;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlmeasure.ControlMeasureExportService;

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

    public void doExportWithOverwrite(int[] controlMeasureIds, String folder, String prefix) throws EmfException {
        doExport(controlMeasureIds, folder, true, prefix);
    }

    public void doExportWithoutOverwrite(int[] controlMeasureIds, String folder, String prefix) throws EmfException {
        doExport(controlMeasureIds, folder, false, prefix);
    }

    private void doExport(int[] controlMeasureIds, String folder, boolean overwrite, String prefix) throws EmfException {
        ControlMeasureExportService service = session.controlMeasureExportService();
        
        File dir = new File(folder);
        if (dir.isDirectory())
            lastFolder = folder;
        else 
            throw new EmfException("Export folder does not exist: " + folder);
        
        session.setMostRecentExportFolder(folder);
        
        if (overwrite)
            service.exportControlMeasuresWithOverwrite(folder, prefix, controlMeasureIds, session.user());
        else
            service.exportControlMeasures(folder, prefix, controlMeasureIds, session.user());
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
