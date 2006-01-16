package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EditToken;

public class EditableDataViewPresenter {

    private EditableDataView view;

    private DataEditorService service;

    private Version version;

    private String table;

    private EditToken token;

    private EditableTablePresenter tablePresenter;

    private EmfSession session;

    public EditableDataViewPresenter(EmfSession session, Version version, String table, EditableDataView view, DataEditorService service) {
        this.session = session;
        this.version = version;
        this.table = table;
        this.view = view;
        this.service = service;
    }

    public void display() throws EmfException {
        token = new EditToken(version, table);
        token.setUser(session.user());
        
        token = service.openSession(token);
        if(!token.isLocked(session.user())) {
            view.notifyLockFailure(token);
            return;
        }
            
        view.observe(this);
        view.display(version, table, service);
    }

    public void displayTable(EditableTableView tableView) throws EmfException {
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
