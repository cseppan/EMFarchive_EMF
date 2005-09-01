package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.services.DataServices;

public class DatasetsBrowserPresenter {

    private DataServices model;
    private DatasetsBrowserView view;

    public DatasetsBrowserPresenter(DataServices model, DatasetsBrowserView view) {
        this.model = model;
        this.view = view;
    }

    public void observe() {
        view.setObserver(this);
    }

    public void notifyCloseView() {
        view.close();
    }

}
