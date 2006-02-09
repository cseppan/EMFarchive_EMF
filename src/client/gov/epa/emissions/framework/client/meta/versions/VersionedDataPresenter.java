package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.DataViewService;
import gov.epa.emissions.framework.services.EmfDataset;

public class VersionedDataPresenter {

    private VersionedDataView view;

    private EmfDataset dataset;

    private DataEditorService editService;

    private DataViewService viewService;

    private User user;

    public VersionedDataPresenter(User user, EmfDataset dataset, DataEditorService service, DataViewService viewService) {
        this.user = user;
        this.dataset = dataset;
        this.editService = service;
        this.viewService = viewService;
    }

    public VersionedDataPresenter(User user, EmfDataset dataset, EmfSession session) {
        this.user = user;
        this.dataset = dataset;
    }

    public void display(VersionedDataView view) {
        this.view = view;
        view.observe(this);

        EditVersionsPresenter versionsPresenter = new EditVersionsPresenter(user, dataset, editService, viewService);
        view.display(dataset, versionsPresenter);
    }

    public void doClose() {
        view.close();
    }

}
