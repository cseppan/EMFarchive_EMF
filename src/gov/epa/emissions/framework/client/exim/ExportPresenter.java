package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
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

    public void observe(ExportView view) {
        this.view = view;
        view.register(this);
        view.setMostRecentUsedFolder(session.getMostRecentExportFolder());
    }

    public void notifyExport(EmfDataset[] datasets, String folder) throws EmfException {
        session.setMostRecentExportFolder(folder);

        ExImServices services = session.getExImServices();
        services.startExport(session.getUser(), datasets, folder);
    }

}
