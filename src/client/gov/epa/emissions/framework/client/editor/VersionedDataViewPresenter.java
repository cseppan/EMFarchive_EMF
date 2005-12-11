package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorService;

public class VersionedDataViewPresenter {

    private VersionedDataView view;

    private DataEditorService services;

    private Version version;

    private String table;

    public VersionedDataViewPresenter(Version version, String table, VersionedDataView view, DataEditorService services) {
        this.version = version;
        this.table = table;
        this.view = view;
        this.services = services;
    }

    public void display() {
        view.display(version, table);
    }

    public void doClose() throws EmfException {
        services.close();
        view.close();
    }

}
