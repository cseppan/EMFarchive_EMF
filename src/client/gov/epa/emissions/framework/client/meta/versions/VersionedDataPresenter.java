package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfDataset;

public class VersionedDataPresenter {

    private VersionedDataView view;

    private EmfDataset dataset;

    private EmfSession session;

    public VersionedDataPresenter(EmfDataset dataset, EmfSession session) {
        this.dataset = dataset;
        this.session = session;
    }

    public void display(VersionedDataView view) {
        this.view = view;
        view.observe(this);

        EditVersionsPresenter versionsPresenter = new EditVersionsPresenter(dataset, session);
        view.display(dataset, versionsPresenter);
    }

    public void doClose() {
        view.close();
    }

}
