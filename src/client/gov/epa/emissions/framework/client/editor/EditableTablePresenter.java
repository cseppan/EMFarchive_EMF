package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorService;

public class EditableTablePresenter implements TablePresenter {

    private EditableTableView view;

    private TablePresenterDelegate delegate;

    private DataEditorService service;

    public EditableTablePresenter(Version version, String table, EditableTableView view, DataEditorService service) {
        this.service = service;
        this.view = view;

        delegate = new TablePresenterDelegate(version, table, view, service);
    }

    public void observe() {
        view.observe(this);
    }

    public void doDisplayNext() throws EmfException {
        submitChanges();
        delegate.doDisplayNext();
    }

    public void doDisplayPrevious() throws EmfException {
        submitChanges();
        delegate.doDisplayPrevious();
    }

    public void doDisplay(int pageNumber) throws EmfException {
        submitChanges();
        delegate.doDisplay(pageNumber);
    }

    public void doDisplayFirst() throws EmfException {
        submitChanges();
        delegate.doDisplayFirst();
    }

    public void doDisplayLast() throws EmfException {
        submitChanges();
        delegate.doDisplayLast();
    }

    public void doDisplayPageWithRecord(int record) throws EmfException {
        submitChanges();
        delegate.doDisplayPageWithRecord(record);
    }

    public int totalRecords() throws EmfException {
        return delegate.totalRecords();
    }

    private void submitChanges() throws EmfException {
        ChangeSet changeset = view.changeset();
        if (changeset.hasChanges())
            service.submit(delegate.editToken(), changeset, delegate.pageNumber());
    }

}
