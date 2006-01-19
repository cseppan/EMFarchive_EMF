package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.editor.DataView;
import gov.epa.emissions.framework.client.editor.DataViewPresenter;
import gov.epa.emissions.framework.services.DataAccessService;
import gov.epa.emissions.framework.services.EmfDataset;

public class VersionsViewPresenter {

    private DataAccessService service;

    private EmfDataset dataset;

    public VersionsViewPresenter(EmfDataset dataset, DataAccessService service) {
        this.dataset = dataset;
        this.service = service;
    }

    public void display(VersionsView view) throws EmfException {
        view.observe(this);

        Version[] versions = service.getVersions(dataset.getDatasetid());
        view.display(versions, dataset.getInternalSources());
    }

    public void doView(Version version, String table, DataView view) throws EmfException {
        if (!version.isFinalVersion())
            throw new EmfException("Can only view a 'final' Version");

        DataViewPresenter presenter = new DataViewPresenter(version, table, view, service);
        presenter.display();
    }

}
