package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.editor.DataView;
import gov.epa.emissions.framework.client.editor.DataViewPresenter;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;

public class VersionsViewPresenter {

    private DataEditorService service;

    private EmfDataset dataset;

    public VersionsViewPresenter(EmfDataset dataset, DataEditorService service) {
        this.dataset = dataset;
        this.service = service;
    }

    public void display(VersionsView view) throws EmfException {
        view.observe(this);

        Version[] versions = service.getVersions(dataset.getDatasetid());
        view.display(versions, dataset.getInternalSources());
    }

    public void doView(Version version, String table, DataView view) throws EmfException {
        DataViewPresenter presenter = new DataViewPresenter(version, table, view, service);
        presenter.display();
    }

}
