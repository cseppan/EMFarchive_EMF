package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.services.data.Note;

public interface NotesTabView {

    void display(Note[] notes, NotesTabPresenter presenter);

}
