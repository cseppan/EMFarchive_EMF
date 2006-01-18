package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorService;

public class EditableTablePresenter implements TablePresenter {

    private EditablePageManagerView view;

    private TablePaginator paginator;

    private DataEditorService service;

    public EditableTablePresenter(Version version, String table, EditablePageManagerView view, DataEditorService service) {
        this.service = service;
        this.view = view;

        paginator = new TablePaginator(version, table, view, service);
    }

    public void observe() {
        view.observe(this);
    }

    public void doDisplayNext() throws EmfException {
        submitChanges();
        paginator.doDisplayNext();
    }

    public void doDisplayPrevious() throws EmfException {
        submitChanges();
        paginator.doDisplayPrevious();
    }

    public void doDisplay(int pageNumber) throws EmfException {
        submitChanges();
        paginator.doDisplay(pageNumber);
    }

    public void doDisplayFirst() throws EmfException {
        submitChanges();
        paginator.doDisplayFirst();
    }

    public void doDisplayLast() throws EmfException {
        submitChanges();
        paginator.doDisplayLast();
    }

    public void doDisplayPageWithRecord(int record) throws EmfException {
        submitChanges();
        paginator.doDisplayPageWithRecord(record);
    }

    public int totalRecords() throws EmfException {
        return paginator.totalRecords();
    }

    void submitChanges() throws EmfException {
        ChangeSet changeset = view.changeset();
        if (changeset.hasChanges()) {
            service.submit(paginator.editToken(), changeset, paginator.pageNumber());
            changeset.clear();
        }
    }

}
