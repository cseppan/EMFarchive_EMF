package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetVersion;
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
        view.setMostRecentUsedFolder(getLastFolder());

        view.display();
    }

    public String getLastFolder() {
        return (lastFolder != null) ? lastFolder : getDefaultFolder();
    }

    public void doExport(EmfDataset[] datasets, Version[] versions, String folder, String rowFilters, DatasetVersion filterDatasetVersion, String filterDatasetJoinCondition, 
            String colOrders, String purpose, boolean overwrite) throws EmfException {
      
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

           services.exportDatasetids(session.user(), datasetIds, versions, folder, overwrite, 
                   rowFilters, (filterDatasetVersion != null ? filterDatasetVersion.getDataset() : null), (filterDatasetVersion != null ? filterDatasetVersion.getVersion() : null), filterDatasetJoinCondition, colOrders, purpose);
           
        } catch (Exception e) {
            // NOTE Auto-generated catch block
            //e.printStackTrace();
            //System.out.println("Exporting datasets failed.");
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

    public Version[] getVersions(EmfDataset dataset) throws EmfException {
        if (dataset == null) {
            return new Version[0];
        }
        return session.dataEditorService().getVersions(dataset.getId());
    }

}
