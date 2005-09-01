package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
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

    public void notifyCloseView() {
        view.close();
    }

    public void notifyExport(EmfDataset dataset) throws EmfException {
        ExportPresenter presenter = new ExportPresenter(user, serviceLocator.getEximServices());
        view.showExport(dataset, presenter);
    }

}
