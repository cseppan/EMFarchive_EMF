package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImService;

import java.io.File;
import java.util.Date;

public class DefaultExportPresenter implements ExportPresenter {

    private ExportView view;

    private EmfSession session;
    
    private static String lastFolder = null;
    
    public DefaultExportPresenter(EmfSession session) {
        this.session = session;
    }

    public void notifyDone() {
        view.close();
    }

    public void display(ExportView view) {
        this.view = view;
        view.observe(this);
        String defaultBaseFolder = "";
        try {
            if (lastFolder == null)
            {
               defaultBaseFolder = getDefaultBaseFolder();
            }
            else
            {
                defaultBaseFolder = lastFolder;
            }
         } catch (EmfException e) {
            System.err.println(e.getMessage());
        }
        view.setMostRecentUsedFolder(defaultBaseFolder);

        view.display();
    }

    public void doExportWithOverwrite(EmfDataset[] datasets, String folder, String purpose) throws EmfException {
        doExport(datasets, folder, true, purpose);
    }

    public void doExport(EmfDataset[] datasets, String folder, String purpose) throws EmfException {
        doExport(datasets, folder, false, purpose);
    }

    private void doExport(EmfDataset[] datasets, String folder, boolean overwrite, String purpose) throws EmfException {
        for (int i = 0; i < datasets.length; i++) {
            datasets[i].setAccessedDateTime(new Date());
        }
        session.setMostRecentExportFolder(folder);
        
        File dir = new File(folder);
        if (dir.isDirectory()) lastFolder = folder;

        ExImService services = session.eximService();
        if (overwrite)
            services.startExportWithOverwrite(session.user(), datasets, translateToServerDir(folder), purpose);
        else
            services.startExport(session.user(), datasets, translateToServerDir(folder), purpose);
    }

//    private String getDefaultBaseFolderForImport() {
//        return session.getMostRecentExportFolder();
//    }
    
    private String translateToServerDir(String dir) throws EmfException {
        if(dir.equalsIgnoreCase(getDefaultBaseFolder()))
            return session.preferences().getServerOutputDir();
        
        return dir;
    }
    
    private String getDefaultBaseFolder() throws EmfException {
        String defaultExportFolder = session.preferences().getOutputDir();
        if (defaultExportFolder != null)
        {
            File tempDir = new File(defaultExportFolder);
            if (!tempDir.isDirectory()) {
                throw new EmfException("Default export directory does not exist ("+defaultExportFolder+")");
            }
        }
        return defaultExportFolder;
    }    
}
