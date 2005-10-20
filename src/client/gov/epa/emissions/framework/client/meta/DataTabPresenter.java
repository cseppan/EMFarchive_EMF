package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.EmfDataset;

public class DataTabPresenter {

    private DataTabView view;
    private EmfDataset dataset;

    public DataTabPresenter(DataTabView view, EmfDataset dataset) {
        this.view = view;
        this.dataset = dataset;
    }

    public void doSave() {
        // No Op
    }

    public void doDisplay() {
        view.displayInternalSources(dataset.getInternalSources());
    }

}
