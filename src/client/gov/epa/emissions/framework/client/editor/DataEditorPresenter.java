package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataEditorService;

public class DataEditorPresenter {

    private DataEditorView view;

    private DataEditorService service;

    private Version version;

    private String table;

    private DataAccessToken token;

    private EditableTablePresenter tablePresenter;

    private User user;

    public DataEditorPresenter(User user, Version version, String table, DataEditorService service) {
        this.user = user;
        this.version = version;
        this.table = table;
        this.service = service;
    }

    public void display(DataEditorView view) throws EmfException {
        token = new DataAccessToken(version, table);
        token = service.openSession(user, token);

        this.view = view;
        view.observe(this);
        view.display(version, table, service);
        view.updateLockPeriod(token.lockStart(), token.lockEnd());
    }

    public void displayTable(EditablePageManagerView tableView) throws EmfException {
        tablePresenter = new EditableTablePresenter(version, table, tableView, service);
        tablePresenter.observe();
        tablePresenter.doDisplayFirst();
    }

    public void doClose() throws EmfException {
        service.closeSession(token);
        view.close();
    }

    public void doDiscard() throws EmfException {
        service.discard(token);
    }

    public void doSave() throws EmfException {
        tablePresenter.submitChanges();
        service.save(token);
    }

}
