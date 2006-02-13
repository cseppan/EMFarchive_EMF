package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.DataViewService;
import gov.epa.emissions.framework.services.Note;

public class DataViewPresenter {

    private DataView view;

    private Version version;

    private String table;

    private DataAccessToken token;

    private EmfSession session;

    public DataViewPresenter(Version version, String table, DataView view, EmfSession session) {
        this.version = version;
        this.table = table;
        this.view = view;
        this.session = session;

        token = new DataAccessToken(version, table);
    }

    private DataViewService viewService() {
        return session.dataViewService();
    }

    private DataCommonsService commonsService() {
        return session.dataCommonsService();
    }

    public void display() throws EmfException {
        viewService().openSession(token);
        view.observe(this);
        view.display(version, table, viewService());
    }

    public void doClose() throws EmfException {
        viewService().closeSession(token);
        view.close();
    }

    public void doAdd(Note note) throws EmfException {
        commonsService().addNote(note);
    }

}
