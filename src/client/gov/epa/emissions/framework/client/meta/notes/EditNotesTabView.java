package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.services.data.DatasetNote;

public interface EditNotesTabView {

    void display(DatasetNote[] notes, EditNotesTabPresenter presenter);

    DatasetNote[] additions();

    void addNote(DatasetNote note);

}
