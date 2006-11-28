package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.notes.NewNoteView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.services.data.NoteType;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataEditorService;

import java.util.Date;

public class DataEditorPresenterImpl implements DataEditorPresenter {

    DataEditorView view;

    private Version version;

    private String table;

    private DataAccessToken token;

    private EditableTablePresenter tablePresenter;

    private EmfSession session;

    private EmfDataset dataset;

    private boolean changesSaved;

    public DataEditorPresenterImpl(EmfDataset dataset, Version version, String table, EmfSession session) {
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

    private void display(DataAccessToken token, DataEditorView view) throws EmfException {
        this.view = view;
        view.observe(this);
        displayView(view);

        view.updateLockPeriod(token.lockStart(), token.lockEnd());
    }

    private void displayView(DataEditorView view) throws EmfException {
        TableMetadata tableMetadata = dataEditorService().getTableMetadata(table);
        Note[] notes = commonsService().getNotes(dataset.getId());

        view.display(version, table, session.user(), tableMetadata, notes);
    }

    public void displayTable(EditorPanelView tableView) throws EmfException {
        tablePresenter = new EditableTablePresenterImpl(dataset.getDatasetType(), token, tableView.tableMetadata(),
                tableView, dataEditorService(), this);
        displayTable(tablePresenter);
    }

    void displayTable(EditableTablePresenter tablePresenter) throws EmfException {
        tablePresenter.display();
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
        discard(dataEditorService(), token, tablePresenter);
        reset(view);
    }

    private void reset(DataEditorView view) {
        view.resetChanges();
        view.disableSaveDiscard();
    }

    void discard(DataEditorService service, DataAccessToken token, EditableTablePresenter tablePresenter)
            throws EmfException {
        service.discard(token);
        tablePresenter.reloadCurrent();
        clearChangesSaved();
    }

    public void doSave() throws EmfException {
        save(view, token, tablePresenter, dataEditorService(), closingRule());
    }

    void save(DataEditorView view, DataAccessToken token, EditableTablePresenter tablePresenter,
            DataEditorService service, ClosingRule closingRule) throws EmfException {

        tablePresenter.submitChanges();
        Date currentDate = new Date();
        dataset.setModifiedDateTime(currentDate);
        token.getVersion().setLastModifiedDate(currentDate);

        try {
            token = service.save(token, dataset, version);
            tablePresenter.reloadCurrent();
            view.updateLockPeriod(token.lockStart(), token.lockEnd());
            changesSaved = true;
            reset(view);
        } catch (EmfException e) {
            clearChangesSaved();
            view.notifySaveFailure(e.getMessage());
            discard(service, token, tablePresenter);
            closingRule.proceedWithClose(areChangesSaved());
        }
    }

    private void clearChangesSaved() {
        changesSaved = false;
    }

    public void doAddNote(NewNoteView view) throws EmfException {
        NoteType[] types = commonsService().getNoteTypes();
        Version[] versions = dataEditorService().getVersions(dataset.getId());
        Note[] notes = commonsService().getNotes(dataset.getId());

        addNote(view, session.user(), dataset, notes, types, versions);
    }

    private DataCommonsService commonsService() {
        return session.dataCommonsService();
    }

    void addNote(NewNoteView view, User user, EmfDataset dataset, Note[] notes, NoteType[] types, Version[] versions)
            throws EmfException {
        view.display(user, dataset, version, notes, types, versions);
        if (view.shouldCreate())
            commonsService().addNote(view.note());
    }

}
