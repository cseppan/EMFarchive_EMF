package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataViewService;

public class DataViewPresenter {

    private DataView view;

    private DataViewService service;

    private Version version;

    private String table;

    private DataAccessToken token;

    public DataViewPresenter(Version version, String table, DataView view, DataViewService service) {
        this.version = version;
        this.table = table;
        this.view = view;
        this.service = service;

        token = new DataAccessToken(version, table);
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
