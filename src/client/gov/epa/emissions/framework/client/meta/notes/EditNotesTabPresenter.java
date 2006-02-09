package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Note;

public class EditNotesTabPresenter {

    private EmfDataset dataset;

    private DataCommonsService service;

    public EditNotesTabPresenter(EmfDataset dataset, DataCommonsService service) {
        this.dataset = dataset;
        this.service = service;
    }

    public void display(NotesTabView view) throws EmfException {
        Note[] notes = service.getNotes(dataset.getId());
        view.display(notes);
    }

    public void doAdd(Note note) throws EmfException {
        service.addNote(note);
    }
}
