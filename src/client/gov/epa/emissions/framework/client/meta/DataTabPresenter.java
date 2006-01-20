package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.DataViewService;
import gov.epa.emissions.framework.services.EmfDataset;

public class DataTabPresenter {

    private DataTabView view;

    private EmfDataset dataset;

    private DataViewService service;

    public DataTabPresenter(DataTabView view, EmfDataset dataset, DataViewService service) {
        this.view = view;
        this.dataset = dataset;
        this.service = service;
    }

    public void doSave() {
        // No Op
    }

    public void doDisplay() {
        view.display(dataset, service);
    }

}
