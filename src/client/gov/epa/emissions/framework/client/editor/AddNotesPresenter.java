package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.Note;

public class AddNotesPresenter {

    private DataCommonsService service;

    private AddNotesView view;

    public AddNotesPresenter(DataCommonsService service) {
        this.service = service;
    }

    public void display(AddNotesView view) {
        this.view = view;
        view.display();
    }

    public void doAdd(Note note) throws EmfException {
        service.addNote(note);
    }

    public void doCancel() {
        view.close();
    }
}
