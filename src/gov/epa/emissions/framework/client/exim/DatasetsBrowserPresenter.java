package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.ServiceLocator;

public class DatasetsBrowserPresenter {

    private ServiceLocator serviceLocator;

    private DatasetsBrowserView view;

    public DatasetsBrowserPresenter(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    public void observe(DatasetsBrowserView view) {
        this.view = view;
        view.setObserver(this);
    }

    public void notifyCloseView() {
        view.close();
    }

    public void notifyExport(EmfDataset dataset) throws EmfException {
        ExportPresenter presenter = new ExportPresenter(null, serviceLocator.getEximServices());
        view.showExport(dataset, presenter);
    }

}
