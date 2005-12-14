package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
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
        Version[] versions = dataEditorService.getVersions(dataset.getDatasetid());
        view.displayVersions(versions, dataset.getInternalSources());
    }

}
