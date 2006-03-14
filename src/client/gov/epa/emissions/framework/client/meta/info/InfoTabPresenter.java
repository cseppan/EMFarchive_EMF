package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class InfoTabPresenter {

    private InfoTabView view;

    private EmfDataset dataset;

    public InfoTabPresenter(InfoTabView view, EmfDataset dataset) {
        this.view = view;
        this.dataset = dataset;
    }

    public void doSave() {
        // No Op
    }

    public void doDisplay() {
        DatasetType type = dataset.getDatasetType();
        if (!type.isExternal())
            view.displayInternalSources(dataset.getInternalSources());
        else
            view.displayExternalSources(dataset.getExternalSources());
    }

}
