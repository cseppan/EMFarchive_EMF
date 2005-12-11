package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.DataEditorService;

public class VersionsPresenter {

    private DataEditorService service;

    public VersionsPresenter(DataEditorService service) {
        this.service = service;
    }

    public void doView(Version version, String table, VersionedDataView view) {
        VersionedDataViewPresenter presenter = new VersionedDataViewPresenter(version, table, view, service);
        presenter.display();
    }

}
