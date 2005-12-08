package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;

public class DataTabPresenter {

    private DataTabView view;

    private EmfDataset dataset;

    private DataEditorService dataEditorService;

    public DataTabPresenter(DataTabView view, EmfDataset dataset, DataEditorService dataEditorService) {
        this.view = view;
        this.dataset = dataset;
        this.dataEditorService = dataEditorService;
    }

    public void doSave() {
        // No Op
    }

    public void doDisplay() throws EmfException {
        DatasetType type = dataset.getDatasetType();
        if (!type.isExternal())
            view.displayInternalSources(dataset.getInternalSources());
        else
            view.displayExternalSources(dataset.getExternalSources());

        view.displayVersions(dataEditorService.getVersions(dataset.getDatasetid()));
    }

}
