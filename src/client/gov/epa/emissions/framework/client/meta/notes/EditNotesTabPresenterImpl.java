package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Note;
import gov.epa.emissions.framework.services.data.NoteType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        Note[] combinedNotesList = combinedNotesList(notes, view.additions());
        newNoteView.display(user, dataset, combinedNotesList, types, versions);
        if (newNoteView.shouldCreate())
            view.addNote(newNoteView.note());
    }

    private Note[] combinedNotesList(Note[] a, Note[] b) {
        List list = new ArrayList();
        list.addAll(Arrays.asList(a));
        list.addAll(Arrays.asList(b));

        return (Note[]) list.toArray(new Note[0]);
    }

    public void doViewNote(Note note, NoteView window) {
        window.display(note);
    }
}
