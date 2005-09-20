package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImServices;

public class ExportPresenter {

    private ExportView view;

    private EmfSession session;

    public ExportPresenter(EmfSession session) {
        this.session = session;
    }

    public void notifyDone() {
        view.close();
    }

    public void display(ExportView view) {
        this.view = view;
        view.observe(this);
        view.setMostRecentUsedFolder(session.getMostRecentExportFolder());

        view.display();
    }

    // FIXME: have two separate, explicit methods for overwrite/no overwrite
    public void notifyExport(EmfDataset[] datasets, String folder, boolean overwrite, String description) throws EmfException {
        session.setMostRecentExportFolder(folder);

        ExImServices services = session.getExImServices();
        services.startExport(session.getUser(), datasets, folder, overwrite, description);
    }

}
