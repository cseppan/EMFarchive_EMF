package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.data.TablePaginator;
import gov.epa.emissions.framework.services.DataAccessService;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class ViewableTablePresenter implements TablePresenter {

    private NonEditablePageManagerView view;

    private TablePaginator delegate;

    private DataAccessService service;

    private InternalSource source;

    public ViewableTablePresenter(Version version, String table, InternalSource source,
            NonEditablePageManagerView view, DataAccessService service) {
        delegate = new TablePaginator(version, table, view, service);
        this.source = source;
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
        validateColsInSortOrder(sortOrder, source.getCols());
        Page page = service.applyConstraints(delegate.token(), rowFilter, sortOrder);
        view.display(page);
        view.updateFilteredRecordsCount(delegate.totalRecords());
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
