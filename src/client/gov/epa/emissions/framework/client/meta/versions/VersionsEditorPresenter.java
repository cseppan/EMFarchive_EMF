package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;

public class VersionsEditorPresenter {

    private VersionsEditorView view;

    private EmfDataset dataset;

    private DataEditorService service;

    public VersionsEditorPresenter(EmfDataset dataset, DataEditorService service) {
        this.dataset = dataset;
        this.service = service;
    }

    public void display(VersionsEditorView view) {
        this.view = view;
        view.observe(this);
        view.display(dataset, service);
    }

    public void doClose() {
        view.close();
    }

}
