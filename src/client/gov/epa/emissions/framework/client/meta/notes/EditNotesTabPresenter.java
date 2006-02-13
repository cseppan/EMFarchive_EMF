package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Note;

public class EditNotesTabPresenter {

    private EmfDataset dataset;

    private DataCommonsService service;

    private EditNotesTabView view;

    public EditNotesTabPresenter(EmfDataset dataset, DataCommonsService service, EditNotesTabView view) {
        this.dataset = dataset;
        this.service = service;
        this.view = view;
    }

    public void display() throws EmfException {
        Note[] notes = service.getNotes(dataset.getId());
        view.display(notes);
    }

    public void doSave() throws EmfException {
        Note[] additions = view.additions();
        for (int i = 0; i < additions.length; i++) {
            service.addNote(additions[i]);
        }
    }
}
