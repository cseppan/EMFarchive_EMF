package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.data.TablePaginator;
import gov.epa.emissions.framework.client.data.TablePaginatorImpl;
import gov.epa.emissions.framework.client.data.TablePresenterDelegate;
import gov.epa.emissions.framework.client.data.TablePresenterDelegateImpl;
import gov.epa.emissions.framework.services.DataAccessService;

public class ViewableTablePresenter implements TablePresenter {

    private ViewerPanelView view;

    private TablePresenterDelegate delegate;

    public ViewableTablePresenter(DatasetType datasetType, Version version, String table, TableMetadata tableMetadata,
            ViewerPanelView view, DataAccessService service) {
        this(datasetType, new TablePaginatorImpl(version, table, view, service), tableMetadata, view, service);
    }

    public ViewableTablePresenter(DatasetType datasetType, TablePaginator paginator, TableMetadata tableMetadata,
            ViewerPanelView view, DataAccessService service) {
        this(new TablePresenterDelegateImpl(datasetType, paginator, tableMetadata, view, service), view);
    }

    public ViewableTablePresenter(TablePresenterDelegate delegate, ViewerPanelView view) {
        this.view = view;
        this.delegate = delegate;
    }

    public void display() throws EmfException {
        view.observe(this);
        delegate.display();
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

    public void doApplyConstraints(String rowFilter, String sortOrder) throws EmfException {
        delegate.doApplyConstraints(rowFilter, sortOrder);
    }

}
