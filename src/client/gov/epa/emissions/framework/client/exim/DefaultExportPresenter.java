package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.preference.UserPreferences;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImService;

import java.util.Date;

public class DefaultExportPresenter implements ExportPresenter {

    private ExportView view;

    private EmfSession session;

    public DefaultExportPresenter(EmfSession session) {
        this.session = session;
    }

    public void notifyDone() {
        view.close();
    }

    public void display(ExportView view) {
        this.view = view;
        view.observe(this);
        view.setMostRecentUsedFolder(getDefaultBaseFolderForImport());

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

        ExImService services = session.eximService();
        if (overwrite)
            services.startExportWithOverwrite(session.user(), datasets, folder, purpose);
        else
            services.startExport(session.user(), datasets, folder, purpose);
    }
    
    private String getDefaultBaseFolderForImport() {
        UserPreferences up = UserPreferences.getInstance();
        String preferenceDrive = up.getProperty(UserPreferences.EMF_OUTPUT_DRIVE);
        if(preferenceDrive != null && !preferenceDrive.equals(""))
            return preferenceDrive + ":\\" + up.getProperty(UserPreferences.EMF_DEFAULT_OUTPUT_DIR);
        
        return session.getMostRecentExportFolder();
    }
}
