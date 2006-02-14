package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.notes.NewNoteView;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.NoteType;

public class DataEditorPresenter {

    private DataEditorView view;

    private Version version;

    private String table;

    private DataAccessToken token;

    private EditableTablePresenter tablePresenter;

    private EmfSession session;

    private EmfDataset dataset;

    public DataEditorPresenter(EmfDataset dataset, Version version, String table, EmfSession session) {
        this.dataset = dataset;
        this.version = version;
        this.table = table;
        this.session = session;
    }

    public void display(DataEditorView view) throws EmfException {
        token = new DataAccessToken(version, table);
        token = dataEditorService().openSession(session.user(), token);

        if (!token.isLocked(session.user())) {// abort
            view.notifyLockFailure(token);
            return;
        }

        display(token, view);
    }

    private DataEditorService dataEditorService() {
        return session.dataEditorService();
    }

    private void display(DataAccessToken token, DataEditorView view) {
        this.view = view;
        view.observe(this);
        view.display(version, table, dataEditorService());
        view.updateLockPeriod(token.lockStart(), token.lockEnd());
    }

    public void displayTable(EditablePageManagerView tableView) throws EmfException {
        EditableTablePresenter tablePresenter = new EditableTablePresenterImpl(version, table, tableView,
                dataEditorService());
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

        dataEditorService().closeSession(token);
        view.close();
    }

    private boolean hasChanges() throws EmfException {
        return tablePresenter.hasChanges() || dataEditorService().hasChanges(token);
    }

    public void doDiscard() throws EmfException {
        dataEditorService().discard(token);
    }

    public void doSave() throws EmfException {
        tablePresenter.submitChanges();
        try {
            token = dataEditorService().save(token);
            view.updateLockPeriod(token.lockStart(), token.lockEnd());
        } catch (EmfException e) {
            view.notifySaveFailure(e.getMessage());
            doDiscard();
            doClose();
        }
    }

    public void doAddNote(NewNoteView view) throws EmfException {
        NoteType[] types = commonsService().getNoteTypes();
        Version[] versions = dataEditorService().getVersions(dataset.getId());

        addNote(view, session.user(), dataset, types, versions);
    }

    private DataCommonsService commonsService() {
        return session.dataCommonsService();
    }

    void addNote(NewNoteView view, User user, EmfDataset dataset, NoteType[] types, Version[] versions)
            throws EmfException {
        view.display(user, dataset, types, versions);
        if (view.shouldCreate())
            commonsService().addNote(view.note());
    }
}
