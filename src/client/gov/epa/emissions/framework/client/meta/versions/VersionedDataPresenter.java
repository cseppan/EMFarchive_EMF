package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;

public class VersionedDataPresenter {

    private VersionedDataView view;

    private EmfDataset dataset;

    private DataEditorService service;

    private EmfSession session;

    public VersionedDataPresenter(EmfDataset dataset, EmfSession session, DataEditorService service) {
        this.dataset = dataset;
        this.session = session;
        this.service = service;
    }

    public void display(VersionedDataView view) {
        this.view = view;
        view.observe(this);
        view.display(dataset, session, service);
    }

    public void doClose() {
        view.close();
    }

}
