package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.services.data.Note;

public interface EditNotesTabView {

    void display(Note[] notes, EditNotesTabPresenter presenter);

    Note[] additions();

    void addNote(Note note);

}
