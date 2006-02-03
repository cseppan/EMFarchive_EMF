package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessService;

public class ViewableTablePresenter implements TablePresenter {

    private NonEditableTableView view;

    private TablePaginator delegate;

    private DataAccessService service;

    public ViewableTablePresenter(Version version, String table, NonEditableTableView view, DataAccessService service) {
        delegate = new TablePaginator(version, table, view, service);
        this.view = view;
        this.service = service;
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

    public void applyConstraints(String rowFilter, String sortOrder) throws EmfException {
        Page page = service.applyConstraints(delegate.token(), rowFilter, sortOrder);
        view.display(page);
    }

}
