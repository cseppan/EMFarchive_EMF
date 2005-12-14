package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EditToken;

public class DataViewPresenter {

    private DataView view;

    private DataEditorService service;

    private Version version;

    private String table;

    private EditToken token;

    public DataViewPresenter(Version version, String table, DataView view, DataEditorService service) {
        this.version = version;
        this.table = table;
        token = new EditToken(version, table);
        this.view = view;
        this.service = service;
    }

    public void display() throws EmfException {
        service.openSession(token);
        view.observe(this);
        view.display(version, table, service);
    }

    public void doClose() throws EmfException {
        service.closeSession(token);
        view.close();
    }

}
