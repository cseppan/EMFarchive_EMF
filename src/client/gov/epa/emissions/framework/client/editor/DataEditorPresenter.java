package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
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
    
    public DataEditorPresenter(User user, Version version, String table, EmfSession session) {
        this.user = user;
        this.version = version;
        this.table = table;
        this.service = session.dataEditorService();
    }

    public void display(DataEditorView view) throws EmfException {
        token = new DataAccessToken(version, table);
        token = service.openSession(user, token);

        if (!token.isLocked(user)) {// abort
            view.notifyLockFailure(token);
            return;
        }

        display(token, view);
    }

    private void display(DataAccessToken token, DataEditorView view) {
        this.view = view;
        view.observe(this);
        view.display(version, table, service);
        view.updateLockPeriod(token.lockStart(), token.lockEnd());
    }

    public void displayTable(EditablePageManagerView tableView) throws EmfException {
        EditableTablePresenter tablePresenter = new EditableTablePresenterImpl(version, table, tableView, service);
        displayTable(tablePresenter);
    }

    void displayTable(EditableTablePresenter tablePresenter) throws EmfException {
        this.tablePresenter = tablePresenter;
        tablePresenter.observe();
        tablePresenter.doDisplayFirst();
    }

    public void doClose() throws EmfException {
        if (hasChanges() && !view.confirmDiscardChanges())
            return;

        service.closeSession(token);
        view.close();
    }

    private boolean hasChanges() throws EmfException {
        return tablePresenter.hasChanges() || service.hasChanges(token);
    }

    public void doDiscard() throws EmfException {
        service.discard(token);
    }

    public void doSave() throws EmfException {
        tablePresenter.submitChanges();
        try {
            token = service.save(token);
            view.updateLockPeriod(token.lockStart(), token.lockEnd());
        } catch (EmfException e) {
            view.notifySaveFailure(e.getMessage());
            doDiscard();
            doClose();
        }
    }

}
