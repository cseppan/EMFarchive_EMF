package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
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

        Version[] versions = dataEditorService.getVersions(dataset.getDatasetid());
        view.displayVersions(versions);
    }

    public void doDisplayVersionedTable(Version version, String table, VersionedTableView versionedView)
            throws EmfException {
        VersionedTablePresenter presenter = new VersionedTablePresenter(version, table, versionedView,
                dataEditorService);
        presenter.observeView();
        presenter.doDisplayFirst();
    }

}
