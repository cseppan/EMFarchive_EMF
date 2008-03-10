package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DatasetNote;
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
        DatasetNote[] datasetNotes = service().getDatasetNotes(dataset.getId());
        view.display(datasetNotes, this);
    }

    private DataCommonsService service() {
        return session.dataCommonsService();
    }

    public void doSave() throws EmfException {
        DatasetNote[] additions = view.additions();
        service().addDatasetNotes(additions);
//        for (int i = 0; i < additions.length; i++) {
//            DatasetNote datasetNote = additions[i];
//            datasetNote.setDatasetId(dataset.getId());
//
//            service().addDatasetNote(datasetNote);
//        }
    }

    public void doAddNote(NewNoteView view) throws EmfException {
        NoteType[] types = service().getNoteTypes();
        Version[] versions = session.dataEditorService().getVersions(dataset.getId());
        DatasetNote[] notes = service().getDatasetNotes(dataset.getId());

        addDatasetNote(view, session.user(), dataset, notes, types, versions);
    }

    public void addExistingNotes(AddExistingNotesDialog view) {
     //   Note[] notes = service().getAllNotes("");
        view.observe(this);
        view.display(new Note[]{});
    }
    

    void addDatasetNote(NewNoteView newNoteView, User user, EmfDataset dataset, DatasetNote[] datasetNotes, NoteType[] types,
            Version[] versions) {
        DatasetNote[] combinedNotesList = combinedNotesList(datasetNotes, view.additions());
        newNoteView.display(user, dataset, combinedNotesList, types, versions);
        if (newNoteView.shouldCreate()){
            //service().addDatasetNote(datasetNote);
            view.addNote(newNoteView.DSnote());
        }
    }
    

    private DatasetNote[] combinedNotesList(DatasetNote[] a, DatasetNote[] datasetNotes) {
        List list = new ArrayList();
        list.addAll(Arrays.asList(a));
        list.addAll(Arrays.asList(datasetNotes));

        return (DatasetNote[]) list.toArray(new DatasetNote[0]);
    }

    public void doViewNote(DatasetNote note, NoteView window) {
        window.display(note);
    }
    
    public DatasetNote[] getSelectedNotes(AddExistingNotesDialog dialog) throws EmfException{
        List<Integer> noteIds =new ArrayList<Integer> (); 
        Note[] notes=dialog.getNotes();
        for (Note note: notes)
            noteIds.add(new Integer(note.getId()));
        
        int[] selectedIndexes = new int[noteIds.size()];
        for (int i = 0; i < selectedIndexes.length; i++) {
            selectedIndexes[i] = noteIds.get(i).intValue();
        }
        return SetDSNote(service().getNotes(selectedIndexes));
    }
    
    private DatasetNote[] SetDSNote(Note[] notes){
        List<DatasetNote> dsNotes=new ArrayList<DatasetNote>();
        for (Note note :notes){
            DatasetNote dsNote = new DatasetNote();
            dsNote.setDatasetId(dataset.getId());
            dsNote.setNote(note);
            dsNotes.add(dsNote);
        }
        return dsNotes.toArray(new DatasetNote[0]);
    }

    public Note[] getNotes(String nameContains) throws EmfException {
        return service().getNameContainNotes(nameContains);
    }

}
