package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.editor.NonEditableDataView;
import gov.epa.emissions.framework.client.editor.NonEditableDataViewPresenter;
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

    public void doView(Version version, String table, NonEditableDataView view) throws EmfException {
        NonEditableDataViewPresenter presenter = new NonEditableDataViewPresenter(version, table, view, service);
        presenter.display();
    }

}
