package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.notes.NewNoteView;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.services.DataViewService;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Note;
import gov.epa.emissions.framework.services.NoteType;

public class DataViewPresenter {

    private DataView view;

    private Version version;

    private String table;

    private DataAccessToken token;

    private EmfSession session;

    private EmfDataset dataset;

    public DataViewPresenter(EmfDataset dataset, Version version, String table, DataView view, EmfSession session) {
        this.dataset = dataset;
        this.version = version;
        this.table = table;
        this.view = view;
        this.session = session;

        token = new DataAccessToken(version, table);
    }

    private DataViewService viewService() {
        return session.dataViewService();
    }

    private DataCommonsService commonsService() {
        return session.dataCommonsService();
    }

    public void display() throws EmfException {
        token = viewService().openSession(token);
        view.observe(this);

        TableMetadata tableMetadata = viewService().getTableMetadata(table);
        view.display(version, table, tableMetadata);
    }

    public void displayTable(ViewerPanel tableView) throws EmfException {
        TableMetadata tableMetadata = viewService().getTableMetadata(table);
        TablePresenter tablePresenter = new ViewableTablePresenter(dataset.getDatasetType(), version, table,
                tableMetadata, tableView, viewService());
        displayTable(tablePresenter);
    }

    void displayTable(TablePresenter tablePresenter) throws EmfException {
        tablePresenter.display();
    }

    public void doClose() throws EmfException {
        viewService().closeSession(token);
        view.close();
    }

    public void doAddNote(NewNoteView view) throws EmfException {
        NoteType[] types = commonsService().getNoteTypes();
        Version[] versions = session.dataEditorService().getVersions(dataset.getId());

        addNote(view, session.user(), dataset, types, versions);
    }

    void addNote(NewNoteView view, User user, EmfDataset dataset, NoteType[] types, Version[] versions)
            throws EmfException {
        Note[] notes = commonsService().getNotes(dataset.getId());
        view.display(user, dataset, version, notes, types, versions);
        if (view.shouldCreate())
            commonsService().addNote(view.note());
    }

}
