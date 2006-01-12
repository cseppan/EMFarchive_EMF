package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;

public class DataTabPresenter {

    private DataTabView view;

    private EmfDataset dataset;

    private DataEditorService service;

    public DataTabPresenter(DataTabView view, EmfDataset dataset, DataEditorService service) {
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
