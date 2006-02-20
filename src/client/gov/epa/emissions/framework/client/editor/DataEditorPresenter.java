package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.InternalSource;
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

    DataEditorView view;

    private Version version;

    private String table;

    private DataAccessToken token;

    private EditableTablePresenter tablePresenter;

    private EmfSession session;

    private EmfDataset dataset;

    private boolean changesSaved;

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

    DataEditorService dataEditorService() {
        return session.dataEditorService();
    }

    private void display(DataAccessToken token, DataEditorView view) {
        this.view = view;
        view.observe(this);
        view.display(version, table, session.user(), dataEditorService());
        view.updateLockPeriod(token.lockStart(), token.lockEnd());
    }

    public void displayTable(EditablePageManagerView tableView) throws EmfException {
        InternalSource source = source(table, dataset.getInternalSources());
        EditableTablePresenter tablePresenter = new EditableTablePresenterImpl(version, table, source, tableView,
                dataEditorService());
        displayTable(tablePresenter);
    }

    private InternalSource source(String table, InternalSource[] sources) {
        for (int i = 0; i < sources.length; i++) {
            if (sources[i].getTable().equals(table))
                return sources[i];
        }

        return null;
    }

    void displayTable(EditableTablePresenter tablePresenter) throws EmfException {
        this.tablePresenter = tablePresenter;
        tablePresenter.observe();
        tablePresenter.doDisplayFirst();
    }

    public void doClose() throws EmfException {
        close(closingRule(), areChangesSaved());
    }

    boolean areChangesSaved() {
        return changesSaved;
    }

    void close(ClosingRule closingRule, boolean changesSaved) throws EmfException {
        closingRule.close(changesSaved);
    }

    private ClosingRule closingRule() {
        return new ClosingRule(view, tablePresenter, session, token);
    }

    public void doDiscard() throws EmfException {
        discard(dataEditorService(), token);
    }

    void discard(DataEditorService service, DataAccessToken token) throws EmfException {
        service.discard(token);
        clearChangesSaved();
    }

    public void doSave() throws EmfException {
        save(view, token, tablePresenter, dataEditorService(), closingRule());
    }

    void save(DataEditorView view, DataAccessToken token, EditableTablePresenter tablePresenter,
            DataEditorService service, ClosingRule closingRule) throws EmfException {
        tablePresenter.submitChanges();
        try {
            token = service.save(token);
            view.updateLockPeriod(token.lockStart(), token.lockEnd());
            changesSaved = true;
        } catch (EmfException e) {
            clearChangesSaved();
            view.notifySaveFailure(e.getMessage());
            discard(service, token);
            closingRule.proceedWithClose(areChangesSaved());
        }
    }

    private void clearChangesSaved() {
        changesSaved = false;
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
