package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataServices;

public class DatasetsBrowserPresenter {

    private DatasetsBrowserView view;

    private EmfSession session;

    public DatasetsBrowserPresenter(EmfSession session) {
        this.session = session;
    }

    public void observe(DatasetsBrowserView view) {
        this.view = view;
        view.observe(this);
    }

    public void notifyClose() {
        view.close();
    }

    public void notifyExport(EmfDataset[] datasets) throws EmfException {
        ExportPresenter presenter = new ExportPresenter(session);
        view.showExport(datasets, presenter);
    }

    public void notifyRefresh() throws EmfException {
        DataServices dataServices = session.getDataServices();
        // FIXME: fix the type casting
        view.refresh((EmfDataset[]) dataServices.getDatasets());
    }

}
