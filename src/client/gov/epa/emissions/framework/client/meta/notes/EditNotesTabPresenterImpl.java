package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Note;
import gov.epa.emissions.framework.services.NoteType;

public class EditNotesTabPresenterImpl implements EditNotesTabPresenter {

    private EmfDataset dataset;

    private EditNotesTabView view;

    private EmfSession session;

    public EditNotesTabPresenterImpl(EmfDataset dataset, EmfSession session, EditNotesTabView view) {
        this.dataset = dataset;
        this.session = session;
        this.view = view;
    }

    public void display() throws EmfException {
        Note[] notes = service().getNotes(dataset.getId());
        view.display(notes, this);
    }

    private DataCommonsService service() {
        return session.dataCommonsService();
    }

    public void doSave() throws EmfException {
        Note[] additions = view.additions();
        for (int i = 0; i < additions.length; i++) {
            Note note = additions[i];
            note.setDatasetId(dataset.getId());

            service().addNote(note);
        }
    }

    public void doAddNote(NewNoteView view) throws EmfException {
        NoteType[] types = service().getNoteTypes();
        Version[] versions = session.dataEditorService().getVersions(dataset.getId());
        Note[] notes = service().getNotes(dataset.getId());

        addNote(view, session.user(), dataset, notes, types, versions);
    }

    void addNote(NewNoteView newNoteView, User user, EmfDataset dataset, Note[] notes, NoteType[] types,
            Version[] versions) {
        newNoteView.display(user, dataset, notes, types, versions);
        if (newNoteView.shouldCreate())
            view.addNote(newNoteView.note());

    }
}
