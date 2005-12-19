package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataEditorService;

public class NonEditableTablePresenter implements TablePresenter {

    private NonEditableTableView view;

    private TablePresenterDelegate delegate;

    public NonEditableTablePresenter(Version version, String table, NonEditableTableView view, DataEditorService service) {
        delegate = new TablePresenterDelegate(version, table, view, service);
        this.view = view;
    }

    public void observe() {
        view.observe(this);
    }

    public void doDisplayNext() throws EmfException {
        delegate.doDisplayNext();
    }

    public void doDisplayPrevious() throws EmfException {
        delegate.doDisplayPrevious();
    }

    public void doDisplay(int pageNumber) throws EmfException {
        delegate.doDisplay(pageNumber);
    }

    public void doDisplayFirst() throws EmfException {
        delegate.doDisplayFirst();
    }

    public void doDisplayLast() throws EmfException {
        delegate.doDisplayLast();
    }

    public void doDisplayPageWithRecord(int record) throws EmfException {
        delegate.doDisplayPageWithRecord(record);
    }

    public int totalRecords() throws EmfException {
        return delegate.totalRecords();
    }

}
