package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.client.meta.PropertiesEditorTabPresenter;
import gov.epa.emissions.framework.services.EmfException;

public interface EditNotesTabPresenter extends PropertiesEditorTabPresenter {

    void display() throws EmfException;

    void doAddNote(NewNoteView view) throws EmfException;

}