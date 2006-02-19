package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessToken;
import gov.epa.emissions.framework.services.DataEditorService;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Stub;
import org.jmock.core.constraint.IsInstanceOf;
import org.jmock.core.stub.ReturnStub;

public class EditableTablePresenterTest extends MockObjectTestCase {

    public void testShouldObserveViewOnObserve() {
        Mock view = mock(EditablePageManagerView.class);

        TablePresenter p = new EditableTablePresenterImpl(null, "table", null, (EditablePageManagerView) view.proxy(),
                null);
        view.expects(once()).method("observe").with(same(p));

        p.observe();
    }

    public void testShouldDisplayPageOneAfterApplyingConstraintsOnApplyConstraints() throws EmfException {
        Mock view = mock(EditablePageManagerView.class);
        Mock service = mock(DataEditorService.class);

        Mock source = mock(InternalSource.class);
        String[] cols = { "col1", "col2", "col3" };
        source.stubs().method("getCols").will(returnValue(cols));

        TablePresenter p = new EditableTablePresenterImpl(null, "table", (InternalSource) source.proxy(),
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

    public void testShouldIgnoreWhenSortOrderIsEmptyOnApplyConstraints() throws EmfException {
        Mock view = mock(EditablePageManagerView.class);
        Mock service = mock(DataEditorService.class);

        Mock source = mock(InternalSource.class);
        String[] cols = { "col1", "col2", "col3" };
        source.stubs().method("getCols").will(returnValue(cols));

        TablePresenter p = new EditableTablePresenterImpl(null, "table", (InternalSource) source.proxy(),
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

        TablePresenter p = new EditableTablePresenterImpl(null, "table", (InternalSource) source.proxy(),
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

        TablePresenter p = new EditableTablePresenterImpl(null, "table", (InternalSource) source.proxy(),
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

        TablePresenter p = new EditableTablePresenterImpl(version, "table", null, null, (DataEditorService) services
                .proxy());

        assertEquals(28, p.totalRecords());
    }

    public void testShouldDisplayFirstPageOnFirstNextCall() throws Exception {
        Mock service = mock(DataEditorService.class);
        Page page = new Page();
        service.stubs().method("getPage").with(isA(DataAccessToken.class), eq(new Integer(1))).will(returnValue(page));
        service.stubs().method("getPageCount").with(isA(DataAccessToken.class)).will(returnValue(new Integer(20)));

        Mock view = mock(EditablePageManagerView.class);
        view.expects(once()).method("display").with(eq(page));
        view.stubs().method("changeset").withNoArguments().will(returnValue(new ChangeSet()));

        service.stubs().method("getTotalRecords").will(returnValue(new Integer(20)));
        view.stubs().method("updateFilteredRecordsCount").with(eq(new Integer(20)));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        TablePresenter p = new EditableTablePresenterImpl(new Version(), "table", null, (EditablePageManagerView) view
                .proxy(), (DataEditorService) service.proxy());

        p.doDisplayNext();
    }

    public void testShouldDisplaySpecifiedPage() throws Exception {
        Mock service = mock(DataEditorService.class);
        Page page = new Page();
        service.stubs().method("getPage").with(isA(DataAccessToken.class), eq(new Integer(21))).will(returnValue(page));

        Mock view = mock(EditablePageManagerView.class);
        view.expects(once()).method("display").with(eq(page));
        view.stubs().method("changeset").withNoArguments().will(returnValue(new ChangeSet()));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        service.stubs().method("getTotalRecords").will(returnValue(new Integer(20)));
        view.stubs().method("updateFilteredRecordsCount").with(eq(new Integer(20)));

        TablePresenter p = new EditableTablePresenterImpl(new Version(), "table", null, (EditablePageManagerView) view
                .proxy(), (DataEditorService) service.proxy());

        p.doDisplay(21);
    }

    public void testShouldDisplayPageWithRecord() throws Exception {
        Mock service = mock(DataEditorService.class);
        Page page = new Page();
        service.stubs().method("getPageWithRecord").with(isA(DataAccessToken.class), eq(new Integer(21))).will(
                returnValue(page));

        Mock view = mock(EditablePageManagerView.class);
        view.expects(once()).method("display").with(eq(page));
        view.stubs().method("changeset").withNoArguments().will(returnValue(new ChangeSet()));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        service.stubs().method("getTotalRecords").will(returnValue(new Integer(20)));
        view.stubs().method("updateFilteredRecordsCount").with(eq(new Integer(20)));

        TablePresenter p = new EditableTablePresenterImpl(new Version(), "table", null, (EditablePageManagerView) view
                .proxy(), (DataEditorService) service.proxy());

        p.doDisplayPageWithRecord(21);
    }

    public void testShouldDisplayFirstPage() throws Exception {
        Mock service = mock(DataEditorService.class);
        Page page = new Page();
        service.stubs().method("getPage").with(isA(DataAccessToken.class), eq(new Integer(1))).will(returnValue(page));

        Mock view = mock(EditablePageManagerView.class);
        view.expects(once()).method("display").with(eq(page));
        view.stubs().method("changeset").withNoArguments().will(returnValue(new ChangeSet()));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        service.stubs().method("getTotalRecords").will(returnValue(new Integer(20)));
        view.stubs().method("updateFilteredRecordsCount").with(eq(new Integer(20)));

        TablePresenter p = new EditableTablePresenterImpl(new Version(), "table", null, (EditablePageManagerView) view
                .proxy(), (DataEditorService) service.proxy());

        p.doDisplayFirst();
    }

    public void testShouldDisplayFirstPageEvenAfterPrevRequestOnFirstPage() throws Exception {
        Mock service = mock(DataEditorService.class);
        Page page = new Page();
        page.setNumber(1);
        service.stubs().method("getPage").with(isA(DataAccessToken.class), eq(new Integer(1))).will(returnValue(page));

        Mock view = mock(EditablePageManagerView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page));
        view.stubs().method("changeset").withNoArguments().will(returnValue(new ChangeSet()));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        service.stubs().method("getTotalRecords").will(returnValue(new Integer(20)));
        view.stubs().method("updateFilteredRecordsCount").with(eq(new Integer(20)));

        TablePresenter p = new EditableTablePresenterImpl(new Version(), "table", null, (EditablePageManagerView) view
                .proxy(), (DataEditorService) service.proxy());

        p.doDisplayFirst();
        p.doDisplayPrevious();
    }

    public void testShouldDisplayLastPageEvenAfterNextRequestOnLastPage() throws Exception {
        Mock service = mock(DataEditorService.class);
        Page page = new Page();
        page.setNumber(20);
        service.stubs().method("getPage").with(isA(DataAccessToken.class), eq(new Integer(20))).will(returnValue(page));
        service.stubs().method("getPageCount").with(isA(DataAccessToken.class)).will(returnValue(new Integer(20)));

        Mock view = mock(EditablePageManagerView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page));
        view.stubs().method("changeset").withNoArguments().will(returnValue(new ChangeSet()));
        view.stubs().method("scrollToPageEnd").withNoArguments();

        service.stubs().method("getTotalRecords").will(returnValue(new Integer(20)));
        view.stubs().method("updateFilteredRecordsCount").with(eq(new Integer(20)));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        TablePresenter p = new EditableTablePresenterImpl(new Version(), "table", null, (EditablePageManagerView) view
                .proxy(), (DataEditorService) service.proxy());

        p.doDisplayLast();
        p.doDisplayNext();
    }

    public void testShouldDisplayLastPage() throws Exception {
        Mock service = mock(DataEditorService.class);
        Page page = new Page();
        service.stubs().method("getPage").with(isA(DataAccessToken.class), eq(new Integer(20))).will(returnValue(page));
        service.stubs().method("getPageCount").with(isA(DataAccessToken.class)).will(returnValue(new Integer(20)));

        Mock view = mock(EditablePageManagerView.class);
        view.expects(once()).method("display").with(eq(page));
        view.stubs().method("changeset").withNoArguments().will(returnValue(new ChangeSet()));
        view.stubs().method("scrollToPageEnd").withNoArguments();

        service.stubs().method("getTotalRecords").will(returnValue(new Integer(20)));
        view.stubs().method("updateFilteredRecordsCount").with(eq(new Integer(20)));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        TablePresenter p = new EditableTablePresenterImpl(new Version(), "table", null, (EditablePageManagerView) view
                .proxy(), (DataEditorService) service.proxy());

        p.doDisplayLast();
    }

    public void testShouldDisplaySecondPageOnTwoConsecutiveNextCalls() throws Exception {
        Mock service = mock(DataEditorService.class);
        Page page = new Page();
        page.setNumber(1);
        service.expects(once()).method("getPage").with(isA(DataAccessToken.class), eq(new Integer(1))).will(
                returnValue(page));
        service.stubs().method("getPage").with(isA(DataAccessToken.class), eq(new Integer(2))).will(returnValue(page));
        service.stubs().method("getPageCount").with(isA(DataAccessToken.class)).will(returnValue(new Integer(20)));

        Mock view = mock(EditablePageManagerView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page));
        view.stubs().method("changeset").withNoArguments().will(returnValue(new ChangeSet()));

        service.stubs().method("getTotalRecords").will(returnValue(new Integer(20)));
        view.stubs().method("updateFilteredRecordsCount").with(eq(new Integer(20)));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        TablePresenter p = new EditableTablePresenterImpl(new Version(), "table", null, (EditablePageManagerView) view
                .proxy(), (DataEditorService) service.proxy());

        p.doDisplayNext();
        p.doDisplayNext();
    }

    public void testShouldSubmitChangesOnPage1OnDisplayNextOnPage1() throws Exception {
        Mock service = mock(DataEditorService.class);
        Page page = new Page();
        page.setNumber(1);
        service.expects(once()).method("getPage").with(isA(DataAccessToken.class), eq(new Integer(1))).will(
                returnValue(page));
        service.stubs().method("getPage").with(isA(DataAccessToken.class), eq(new Integer(2))).will(returnValue(page));
        service.stubs().method("getPageCount").with(isA(DataAccessToken.class)).will(returnValue(new Integer(20)));

        Mock view = mock(EditablePageManagerView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page));

        service.stubs().method("getTotalRecords").will(returnValue(new Integer(20)));
        view.stubs().method("updateFilteredRecordsCount").with(eq(new Integer(20)));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        TablePresenter p = new EditableTablePresenterImpl(new Version(), "table", null, (EditablePageManagerView) view
                .proxy(), (DataEditorService) service.proxy());

        // changes
        Stub pageZeroChanges = new ReturnStub(new ChangeSet());

        ChangeSet page1Changes = new ChangeSet();
        page1Changes.addNew(new VersionedRecord());
        Stub pageOneChanges = new ReturnStub(page1Changes);

        view.stubs().method("changeset").withNoArguments().will(onConsecutiveCalls(pageZeroChanges, pageOneChanges));

        service.expects(once()).method("submit").with(new IsInstanceOf(DataAccessToken.class), same(page1Changes),
                eq(new Integer(page.getNumber())));

        p.doDisplayNext();// page 1
        p.doDisplayNext();// page 2
    }

    public void testShouldSubmitChangesOnPage2OnDisplayPreviousOnPage2() throws Exception {
        Mock service = mock(DataEditorService.class);
        Page page2 = new Page();
        page2.setNumber(2);
        service.expects(once()).method("getPage").with(isA(DataAccessToken.class), eq(new Integer(1))).will(
                returnValue(page2));
        service.stubs().method("getPage").with(isA(DataAccessToken.class), eq(new Integer(2))).will(returnValue(page2));
        service.stubs().method("getPageCount").with(isA(DataAccessToken.class)).will(returnValue(new Integer(20)));

        Mock view = mock(EditablePageManagerView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page2));

        service.stubs().method("getTotalRecords").will(returnValue(new Integer(20)));
        view.stubs().method("updateFilteredRecordsCount").with(eq(new Integer(20)));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        TablePresenter p = new EditableTablePresenterImpl(new Version(), "table", null, (EditablePageManagerView) view
                .proxy(), (DataEditorService) service.proxy());

        // changes
        Stub pageZeroChanges = new ReturnStub(new ChangeSet());

        ChangeSet page2Changes = new ChangeSet();
        page2Changes.addNew(new VersionedRecord());
        Stub pageTwoChanges = new ReturnStub(page2Changes);

        view.stubs().method("changeset").withNoArguments().will(onConsecutiveCalls(pageZeroChanges, pageTwoChanges));

        service.expects(once()).method("submit").with(new IsInstanceOf(DataAccessToken.class), same(page2Changes),
                eq(new Integer(page2.getNumber())));

        p.doDisplay(2);// page 2
        p.doDisplayPrevious();// page 1
    }

    public void testShouldDisplayFirstPageOnDisplayPreviousAfterTwoConsecutiveNextCalls() throws Exception {
        Mock service = mock(DataEditorService.class);
        service.stubs().method("getPageCount").with(isA(DataAccessToken.class)).will(returnValue(new Integer(20)));

        Page page1 = new Page();
        page1.setNumber(1);
        service.expects(atLeastOnce()).method("getPage").with(isA(DataAccessToken.class), eq(new Integer(1))).will(
                returnValue(page1));
        Page page2 = new Page();
        page2.setNumber(2);
        service.expects(once()).method("getPage").with(isA(DataAccessToken.class), eq(new Integer(2))).will(
                returnValue(page2));

        Mock view = mock(EditablePageManagerView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page1));
        view.expects(once()).method("display").with(eq(page2));
        view.stubs().method("changeset").withNoArguments().will(returnValue(new ChangeSet()));

        service.stubs().method("getTotalRecords").will(returnValue(new Integer(20)));
        view.stubs().method("updateFilteredRecordsCount").with(eq(new Integer(20)));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        TablePresenter p = new EditableTablePresenterImpl(new Version(), "table", null, (EditablePageManagerView) view
                .proxy(), (DataEditorService) service.proxy());

        p.doDisplayNext();
        p.doDisplayNext();
        p.doDisplayPrevious();
    }
}
