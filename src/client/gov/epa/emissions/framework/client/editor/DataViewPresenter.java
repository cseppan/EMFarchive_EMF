package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.DataViewService;
import gov.epa.emissions.framework.services.Note;

public class DataViewPresenter {

    private DataView view;

    private DataViewService service;

    private Version version;

    private String table;

    private DataAccessToken token;

    private DataCommonsService commonsService;

    public DataViewPresenter(Version version, String table, DataView view, DataViewService service) {
        this.version = version;
        this.table = table;
        this.view = view;
        this.service = service;

        token = new DataAccessToken(version, table);
    }

    public DataViewPresenter(Version version, String table, DataView view, DataViewService viewService,
            DataCommonsService commonsService) {
        this(version, table, view, viewService);
        this.commonsService = commonsService;
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

    public void doAdd(Note note) throws EmfException {
        commonsService.addNote(note);
    }

}
