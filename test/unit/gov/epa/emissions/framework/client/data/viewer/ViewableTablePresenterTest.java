package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.data.TablePaginator;
import gov.epa.emissions.framework.services.DataAccessService;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataEditorService;

import org.jmock.Mock;
import org.jmock.core.constraint.IsInstanceOf;

public class ViewableTablePresenterTest extends EmfMockObjectTestCase {

    public void testShouldObserveViewOnObserve() {
        Mock view = mock(ViewerPanelView.class);

        TablePresenter p = new ViewableTablePresenter(null, "table", null, (ViewerPanelView) view.proxy(), null);
        view.expects(once()).method("observe").with(same(p));

        p.observe();
    }

    public void testShouldDisplayPageOneAfterApplyingConstraintsOnApplyConstraints() throws EmfException {
        Mock view = mock(ViewerPanelView.class);
        Mock service = mock(DataEditorService.class);

        String[] cols = { "col1", "col2", "col3" };
        TableMetadata tableMetadata = tableMetadata(cols);

        TablePresenter p = new ViewableTablePresenter(null, "table", tableMetadata, (ViewerPanelView) view.proxy(),
                (DataAccessService) service.proxy());

        String rowFilter = "rowFilter";
        String sortOrder = "COL2";
        Page page = new Page();
        service.expects(once()).method("applyConstraints").with(new IsInstanceOf(DataAccessToken.class), eq(rowFilter),
                eq(sortOrder)).will(returnValue(page));
        view.expects(once()).method("display").with(same(page));

        Integer filtered = new Integer(10);
        service.stubs().method("getTotalRecords").will(returnValue(filtered));
        view.expects(once()).method("updateFilteredRecordsCount").with(eq(filtered));

        p.doApplyConstraints(rowFilter, sortOrder);
    }

    public void testShouldRaiseExceptionIfInvalidColsAreSpecifiedInSortOrderOnApplyConstraints() {
        Mock view = mock(ViewerPanelView.class);
        Mock service = mock(DataEditorService.class);

        String[] cols = { "col1", "col2", "col3" };
        TableMetadata tableMetadata = tableMetadata(cols);

        TablePresenter p = new ViewableTablePresenter(null, "table", tableMetadata, (ViewerPanelView) view.proxy(),
                (DataAccessService) service.proxy());

        String sortOrder = "invalid-row";
        try {
            p.doApplyConstraints(null, sortOrder);
        } catch (EmfException e) {
            return;
        }

        fail("Should have raised an exception when Sort Order contains invalid cols");
    }

    public void testShouldRaiseExceptionIfOneOfColsInSortOrderIsInvalidOnApplyConstraints() {
        Mock view = mock(ViewerPanelView.class);
        Mock service = mock(DataEditorService.class);

        String[] cols = { "col1", "col2", "col3" };
        TableMetadata tableMetadata = tableMetadata(cols);

        TablePresenter p = new ViewableTablePresenter(null, "table", tableMetadata, (ViewerPanelView) view.proxy(),
                (DataAccessService) service.proxy());

        String sortOrder = "col3, invalid-row";
        try {
            p.doApplyConstraints(null, sortOrder);
        } catch (EmfException e) {
            assertEquals("Sort Order contains an invalid column: invalid-row", e.getMessage());
            return;
        }

        fail("Should have raised an exception when Sort Order contains invalid cols");
    }

    public void testShouldIgnoreWhenSortOrderIsEmptyOnApplyConstraints() throws EmfException {
        Mock view = mock(ViewerPanelView.class);
        Mock service = mock(DataEditorService.class);

        String[] cols = { "col1", "col2", "col3" };
        TableMetadata tableMetadata = tableMetadata(cols);

        TablePresenter p = new ViewableTablePresenter(null, "table", tableMetadata, (ViewerPanelView) view.proxy(),
                (DataAccessService) service.proxy());

        String rowFilter = "rowFilter";
        String sortOrder = "   ";
        service.expects(once()).method("applyConstraints").with(new IsInstanceOf(DataAccessToken.class), eq(rowFilter),
                eq(sortOrder));
        view.expects(once()).method("display");

        Integer filtered = new Integer(10);
        service.stubs().method("getTotalRecords").will(returnValue(filtered));
        view.expects(once()).method("updateFilteredRecordsCount").with(eq(filtered));

        p.doApplyConstraints(rowFilter, sortOrder);
    }

    private TableMetadata tableMetadata(String[] cols) {
        TableMetadata table = new TableMetadata();
        for (int i = 0; i < cols.length; i++) {
            table.addColumnMetaData(new ColumnMetaData(cols[i], "java.lang.String", 10));
        }
        return table;
    }

    public void testShouldFetchTotalRecords() throws Exception {
        Mock service = mock(DataEditorService.class);
        Version version = new Version();
        service.stubs().method("getTotalRecords").with(isA(DataAccessToken.class)).will(returnValue(new Integer(28)));

        TablePresenter p = new ViewableTablePresenter(version, "table", null, null, (DataAccessService) service.proxy());

        assertEquals(28, p.totalRecords());
    }

    public void testShouldDisplaySpecifiedPage() throws Exception {
        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplay");

        TablePresenter p = new ViewableTablePresenter((TablePaginator) paginator.proxy(), null, null, null);
        p.doDisplay(21);
    }

    public void testShouldDisplayPageWithRecord() throws Exception {
        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplayPageWithRecord");

        TablePresenter p = new ViewableTablePresenter((TablePaginator) paginator.proxy(), null, null, null);
        p.doDisplayPageWithRecord(21);
    }

    public void testShouldDisplayFirstPage() throws Exception {
        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplayFirst");

        TablePresenter p = new ViewableTablePresenter((TablePaginator) paginator.proxy(), null, null, null);
        p.doDisplayFirst();
    }

    public void testShouldDisplayPreviousPage() throws Exception {
        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplayPrevious");

        TablePresenter p = new ViewableTablePresenter((TablePaginator) paginator.proxy(), null, null, null);
        p.doDisplayPrevious();
    }

    public void testShouldDisplayNextPage() throws Exception {
        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplayNext");

        TablePresenter p = new ViewableTablePresenter((TablePaginator) paginator.proxy(), null, null, null);
        p.doDisplayNext();
    }

    public void testShouldDisplayLastPage() throws Exception {
        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplayLast");

        TablePresenter p = new ViewableTablePresenter((TablePaginator) paginator.proxy(), null, null,null);
        p.doDisplayLast();
    }

}
