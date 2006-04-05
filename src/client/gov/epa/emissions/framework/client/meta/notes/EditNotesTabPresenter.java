package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.client.meta.PropertiesEditorTabPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.Note;

public interface EditNotesTabPresenter extends PropertiesEditorTabPresenter {

    void display() throws EmfException;

    void doAddNote(NewNoteView view) throws EmfException;

    void doViewNote(Note note, NoteView window);

}