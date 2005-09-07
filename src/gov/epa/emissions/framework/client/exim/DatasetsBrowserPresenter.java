package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.User;

public class DatasetsBrowserPresenter {

    private ServiceLocator serviceLocator;

    private DatasetsBrowserView view;

    private User user;

    public DatasetsBrowserPresenter(User user, ServiceLocator serviceLocator) {
        this.user = user;
        this.serviceLocator = serviceLocator;
    }

    public void observe(DatasetsBrowserView view) {
        this.view = view;
        view.observe(this);
    }

    public void notifyClose() {
        view.close();
    }

    public void notifyExport(EmfDataset[] datasets) throws EmfException {
        ExportPresenter presenter = new ExportPresenter(user, serviceLocator.getEximServices());
        view.showExport(datasets, presenter);
    }

    public void notifyRefresh() throws EmfException {
        DataServices dataServices = serviceLocator.getDataServices();
        //FIXME: fix the type casting
        view.refresh((EmfDataset[])dataServices.getDatasets());
    }

}
