package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorService;

public class DataViewPresenter {

    private DataView view;

    private DataEditorService service;

    private Version version;

    private String table;

    public DataViewPresenter(Version version, String table, DataView view, DataEditorService service) {
        this.version = version;
        this.table = table;
        this.view = view;
        this.service = service;
    }

    public void display() {
        view.observe(this);
        view.display(version, table, service);
    }

    public void doClose() throws EmfException {
        service.close();
        view.close();
    }

}
