package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorService;

public class VersionedDataViewPresenter {

    private VersionedDataView view;

    private DataEditorService service;

    private Version version;

    private String table;

    public VersionedDataViewPresenter(Version version, String table, VersionedDataView view, DataEditorService service) {
        this.version = version;
        this.table = table;
        this.view = view;
        this.service = service;
    }

    public void display() {
        view.observe(this);
        view.display(version, table);
    }

    public void doClose() throws EmfException {
        service.close();
        view.close();
    }

}
