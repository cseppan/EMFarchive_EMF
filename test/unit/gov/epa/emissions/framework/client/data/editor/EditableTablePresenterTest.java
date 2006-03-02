package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.EmfMockObjectTestCase;
import gov.epa.emissions.framework.client.data.TablePaginator;
import gov.epa.emissions.framework.client.data.viewer.TablePresenter;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataEditorService;

import org.jmock.Mock;
import org.jmock.core.constraint.IsInstanceOf;

public class EditableTablePresenterTest extends EmfMockObjectTestCase {

    public void testShouldObserveViewOnObserve() {
        Mock view = mock(EditablePageManagerView.class);

        TablePresenter p = new EditableTablePresenterImpl(null, null, "table", null, (EditablePageManagerView) view
                .proxy(), null);
        view.expects(once()).method("observe").with(same(p));

        p.observe();
    }

    public void testShouldDisplayPageOneAfterApplyingConstraintsOnApplyConstraints() throws EmfException {
        Mock view = mock(EditablePageManagerView.class);
        Mock service = mock(DataEditorService.class);

        String[] cols = { "col1", "col2", "col3" };

        TablePresenter p = new EditableTablePresenterImpl(null, null, "table", tableMetaData(cols),
                (EditablePageManagerView) view.proxy(), (DataEditorService) service.proxy());

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

    private TableMetadata tableMetaData(String[] cols) {
        TableMetadata tableMetadata = new TableMetadata();
        for (int i = 0; i < cols.length; i++) {
            ColumnMetaData col = new ColumnMetaData(cols[i], "java.lang.String", 20);
            tableMetadata.addColumnMetaData(col);
        }
        return tableMetadata;
    }

    public void testShouldIgnoreWhenSortOrderIsEmptyOnApplyConstraints() throws EmfException {
        Mock view = mock(EditablePageManagerView.class);
        Mock service = mock(DataEditorService.class);

        String[] cols = { "col1", "col2", "col3" };

        TablePresenter p = new EditableTablePresenterImpl(null, null, "table", tableMetaData(cols),
                (EditablePageManagerView) view.proxy(), (DataEditorService) service.proxy());

        String rowFilter = "rowFilter";
        String sortOrder = "    ";
        service.expects(once()).method("applyConstraints").with(new IsInstanceOf(DataAccessToken.class), eq(rowFilter),
                eq(sortOrder));
        view.expects(once()).method("display");

        Integer filtered = new Integer(10);
        service.stubs().method("getTotalRecords").will(returnValue(filtered));
        view.expects(once()).method("updateFilteredRecordsCount").with(eq(filtered));

        p.doApplyConstraints(rowFilter, sortOrder);
    }

    public void testShouldRaiseExceptionIfInvalidColsAreSpecifiedInSortOrderOnApplyConstraints() {
        Mock view = mock(EditablePageManagerView.class);
        Mock service = mock(DataEditorService.class);

        Mock source = mock(InternalSource.class);
        String[] cols = { "col1", "col2", "col3" };
        source.stubs().method("getCols").will(returnValue(cols));

        TablePresenter p = new EditableTablePresenterImpl(null, null, "table", tableMetaData(cols),
                (EditablePageManagerView) view.proxy(), (DataEditorService) service.proxy());

        String sortOrder = "invalid-row";
        try {
            p.doApplyConstraints(null, sortOrder);
        } catch (EmfException e) {
            return;
        }

        fail("Should have raised an exception when Sort Order contains invalid cols");
    }

    public void testShouldRaiseExceptionIfOneOfColsInSortOrderIsInvalidOnApplyConstraints() {
        Mock view = mock(EditablePageManagerView.class);
        Mock service = mock(DataEditorService.class);

        Mock source = mock(InternalSource.class);
        String[] cols = { "col1", "col2", "col3" };
        source.stubs().method("getCols").will(returnValue(cols));

        TablePresenter p = new EditableTablePresenterImpl(null, null, "table", tableMetaData(cols),
                (EditablePageManagerView) view.proxy(), (DataEditorService) service.proxy());

        String sortOrder = "col3, invalid-row";
        try {
            p.doApplyConstraints(null, sortOrder);
        } catch (EmfException e) {
            assertEquals("Sort Order contains an invalid column: invalid-row", e.getMessage());
            return;
        }

        fail("Should have raised an exception when Sort Order contains invalid cols");
    }

    public void testShouldFetchTotalRecords() throws Exception {
        Mock services = mock(DataEditorService.class);
        Version version = new Version();
        services.stubs().method("getTotalRecords").with(isA(DataAccessToken.class)).will(returnValue(new Integer(28)));

        TablePresenter p = new EditableTablePresenterImpl(null, version, "table", null, null,
                (DataEditorService) services.proxy());

        assertEquals(28, p.totalRecords());
    }

    public void testShouldDisplayFirstPageOnFirstNextCall() throws Exception {
        Mock view = mockViewWithChanges(20);

        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplayNext");
        stub(paginator, "totalRecords", new Integer(20));

        TablePresenter p = new EditableTablePresenterImpl(null, (TablePaginator) paginator.proxy(), null,
                (EditablePageManagerView) view.proxy(), null);

        p.doDisplayNext();
    }

    public void testShouldDisplaySpecifiedPage() throws Exception {
        Mock view = mockViewWithChanges(20);

        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplay");
        stub(paginator, "totalRecords", new Integer(20));

        TablePresenter p = new EditableTablePresenterImpl(null, (TablePaginator) paginator.proxy(), null,
                (EditablePageManagerView) view.proxy(), null);

        p.doDisplay(21);
    }

    private Mock mockViewWithChanges(int recordsCount) {
        Mock view = mock(EditablePageManagerView.class);
        stub(view, "changeset", new ChangeSet());
        view.stubs().method("updateFilteredRecordsCount").with(eq(new Integer(recordsCount)));

        return view;
    }

    public void testShouldDisplayPageWithRecord() throws Exception {
        Mock view = mockViewWithChanges(20);

        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplayPageWithRecord");
        stub(paginator, "totalRecords", new Integer(20));
        stub(paginator, "isCurrent", Boolean.FALSE);

        TablePresenter p = new EditableTablePresenterImpl(null, (TablePaginator) paginator.proxy(), null,
                (EditablePageManagerView) view.proxy(), null);

        p.doDisplayPageWithRecord(21);
    }

    public void testShouldDisplayFirstPage() throws Exception {
        Mock view = mockViewWithChanges(20);

        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplayFirst");
        stub(paginator, "totalRecords", new Integer(20));

        TablePresenter p = new EditableTablePresenterImpl(null, (TablePaginator) paginator.proxy(), null,
                (EditablePageManagerView) view.proxy(), null);

        p.doDisplayFirst();
    }

    public void testShouldApplyDefaultSortOrderOnDisplay() throws Exception {
        Mock view = mockViewWithChanges(20);

        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "token");
        stub(paginator, "totalRecords", new Integer(20));
        expects(paginator, "doDisplayFirst");

        Mock datasetType = mock(DatasetType.class);
        String sortOrder = "sort-order";
        stub(datasetType, "getDefaultSortOrder", sortOrder);

        Mock service = mock(DataEditorService.class);
        TableMetadata tableMetaData = tableMetaData(new String[] { sortOrder });

        EditableTablePresenter p = new EditableTablePresenterImpl((DatasetType) datasetType.proxy(),
                (TablePaginator) paginator.proxy(), tableMetaData, (EditablePageManagerView) view.proxy(),
                (DataEditorService) service.proxy());

        Page page = new Page();
        service.expects(once()).method("applyConstraints").with(ANYTHING, eq(""), eq(sortOrder))
                .will(returnValue(page));
        view.expects(once()).method("display").with(same(page));

        p.doDisplay();
    }

    public void testShouldDisplayLastPage() throws Exception {
        Mock view = mockViewWithChanges(20);
        view.stubs().method("scrollToPageEnd").withNoArguments();

        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "doDisplayLast");
        stub(paginator, "totalRecords", new Integer(20));

        TablePresenter p = new EditableTablePresenterImpl(null, (TablePaginator) paginator.proxy(), null,
                (EditablePageManagerView) view.proxy(), null);

        p.doDisplayLast();
    }

    public void testShouldReloadCurrentPage() throws Exception {
        Mock view = mockViewWithChanges(20);

        Mock paginator = mock(TablePaginator.class);
        expects(paginator, "reloadCurrent");
        stub(paginator, "totalRecords", new Integer(20));

        EditableTablePresenter p = new EditableTablePresenterImpl(null, (TablePaginator) paginator.proxy(), null,
                (EditablePageManagerView) view.proxy(), null);

        p.reloadCurrent();
    }

}
