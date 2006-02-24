package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.data.TablePaginator;
import gov.epa.emissions.framework.services.DataAccessService;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class ViewableTablePresenter implements TablePresenter {

    private PageManagerView view;

    private TablePaginator delegate;

    private DataAccessService service;

    private TableMetadata tableMetadata;

    public ViewableTablePresenter(Version version, String table, TableMetadata tableMetadata,
            PageManagerView view, DataAccessService service) {
        delegate = new TablePaginator(version, table, view, service);
        this.tableMetadata = tableMetadata;
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

    public void doApplyConstraints(String rowFilter, String sortOrder) throws EmfException {
        validateColsInSortOrder(sortOrder, cols());
        Page page = service.applyConstraints(delegate.token(), rowFilter, sortOrder);
        view.display(page);
        view.updateFilteredRecordsCount(delegate.totalRecords());
    }

    private String[] cols() {
        ColumnMetaData[] cols = tableMetadata.getCols();
        String[] colNames = new String[cols.length];
        for (int i = 0; i < cols.length; i++) {
            colNames[i] = cols[i].getName();
        }
        return colNames;
    }

    private void validateColsInSortOrder(String sortOrder, String[] cols) throws EmfException {
        List colsList = Arrays.asList(cols);
        for (StringTokenizer tokenizer = new StringTokenizer(sortOrder.trim(), ","); tokenizer.hasMoreTokens();) {
            String col = tokenizer.nextToken().trim().toLowerCase();
            if (!colsList.contains(col) && !colsList.contains(col.toUpperCase()))
                throw new EmfException("Sort Order contains an invalid column: " + col);
        }
    }

}
