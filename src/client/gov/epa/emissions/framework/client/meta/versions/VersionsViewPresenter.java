package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.editor.DataView;
import gov.epa.emissions.framework.client.editor.DataViewPresenter;
import gov.epa.emissions.framework.services.DataViewService;
import gov.epa.emissions.framework.services.EmfDataset;

public class VersionsViewPresenter {

    private EmfDataset dataset;

    private EmfSession session;

    public VersionsViewPresenter(EmfDataset dataset, EmfSession session) {
        this.dataset = dataset;
        this.session = session;
    }

    public void display(VersionsView view) throws EmfException {
        view.observe(this);

        DataViewService service = session.dataViewService();
        Version[] versions = service.getVersions(dataset.getId());
        view.display(versions, dataset.getInternalSources());
    }

    public void doView(Version version, String table, DataView view) throws EmfException {
        if (!version.isFinalVersion())
            throw new EmfException("Can only view a 'final' Version");

        DataViewPresenter presenter = new DataViewPresenter(version, table, view, session);
        presenter.display();
    }

}
