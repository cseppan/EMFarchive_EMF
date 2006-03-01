package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.EmfException;

public interface EditNotesTabPresenter {

    void display() throws EmfException;

    void doSave() throws EmfException;

    void doAddNote(NewNoteView view) throws EmfException;

}