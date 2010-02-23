package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.exim.ExImService;

import java.io.File;

public class ExportPresenterImpl implements ExportPresenter {

    private ExportView view;

    private EmfSession session;

    private static String lastFolder=null;

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
        doExportInvoke(datasets, folder, true, purpose);
    }

    public void doExport(EmfDataset[] datasets, String folder, String purpose) throws EmfException {
        doExportInvoke(datasets, folder, false, purpose);
    }

    /**
     * This method was modified on 07/13/2007 to convert the calls to use datasetIds instead of
     * sending the selected datasets back as a collection for export.
     * Original code is preserved as comments in the method.
     */
    private void doExportInvoke(EmfDataset[] datasets, String folder, boolean overwrite, String purpose) throws EmfException {
        ExImService services;
        try {
            services = session.eximService();
            Integer[] datasetIds = new Integer[datasets.length];
            
            for (int i = 0; i < datasets.length; i++)
                datasetIds[i] = new Integer(datasets[i].getId());
            
            session.setMostRecentExportFolder(folder);

            File dir = new File(folder);
            if (dir.isDirectory())
                lastFolder = folder;

            if (overwrite)
                services.exportDatasetidsWithOverwrite(session.user(), datasetIds, folder, purpose);
            else
                services.exportDatasetids(session.user(), datasetIds, folder, purpose);
        } catch (Exception e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
            System.out.println("Exporting datasets failed.");
            throw new EmfException(e.getMessage());
        }
        
 
    }
    
    public void setLastFolder( String lastfolder) {
        lastFolder =lastfolder;
    }

    private String getDefaultFolder() {
        String folder = session.preferences().outputFolder();
        
        if (folder == null || folder.trim().isEmpty())
            folder = "";// default, if unspecified

        return folder;
    }
}
