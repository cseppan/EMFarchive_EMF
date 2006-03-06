package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.data.TablePaginator;
import gov.epa.emissions.framework.client.data.TablePaginatorImpl;
import gov.epa.emissions.framework.services.DataAccessService;

import java.util.StringTokenizer;

public class ViewableTablePresenter implements TablePresenter {

    private ViewerPanelView view;

    private TablePaginator paginator;

    private DataAccessService service;

    private TableMetadata tableMetadata;

    private DatasetType datasetType;

    public ViewableTablePresenter(DatasetType datasetType, Version version, String table, TableMetadata tableMetadata,
            ViewerPanelView view, DataAccessService service) {
        this(datasetType, new TablePaginatorImpl(version, table, view, service), tableMetadata, view, service);
    }

    public ViewableTablePresenter(DatasetType datasetType, TablePaginator paginator, TableMetadata tableMetadata,
            ViewerPanelView view, DataAccessService service) {
        this.datasetType = datasetType;
        this.paginator = paginator;
        this.tableMetadata = tableMetadata;
        this.view = view;
        this.service = service;
    }

    public void display() throws EmfException {
        view.observe(this);
        paginator.doDisplayFirst();
        doApplyConstraints("", datasetType.getDefaultSortOrder());
    }

    public void doDisplayNext() throws EmfException {
        paginator.doDisplayNext();
    }

    public void doDisplayPrevious() throws EmfException {
        paginator.doDisplayPrevious();
    }

    public void doDisplay(int pageNumber) throws EmfException {
        paginator.doDisplay(pageNumber);
    }

    public void doDisplayFirst() throws EmfException {
        paginator.doDisplayFirst();
    }

    public void doDisplayLast() throws EmfException {
        paginator.doDisplayLast();
    }

    public void doDisplayPageWithRecord(int record) throws EmfException {
        paginator.doDisplayPageWithRecord(record);
    }

    public int totalRecords() throws EmfException {
        return paginator.totalRecords();
    }

    public void doApplyConstraints(String rowFilter, String sortOrder) throws EmfException {
        validateColsInSortOrder(sortOrder);
        Page page = service.applyConstraints(paginator.token(), rowFilter, sortOrder);
        view.display(page);
        view.updateFilteredRecordsCount(paginator.totalRecords());
    }

    private void validateColsInSortOrder(String sortOrder) throws EmfException {
        for (StringTokenizer tokenizer = new StringTokenizer(sortOrder.trim(), ","); tokenizer.hasMoreTokens();) {
            String col = tokenizer.nextToken().trim().toLowerCase();
            if (!tableMetadata.containsCol(col))
                throw new EmfException("Sort Order contains an invalid column: " + col);
        }
    }

}
