package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataAccessService;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.DataAccessToken;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class ViewableTablePresenterTest extends MockObjectTestCase {

    public void testShouldObserveViewOnObserve() {
        Mock view = mock(NonEditablePageManagerView.class);

        TablePresenter p = new ViewableTablePresenter(null, "table", null, (NonEditablePageManagerView) view.proxy(),
                null);
        view.expects(once()).method("observe").with(same(p));

        p.observe();
    }

    public void testShouldDisplayPageOneAfterApplyingConstraintsOnApplyConstraints() throws EmfException {
        Mock view = mock(NonEditablePageManagerView.class);
        Mock service = mock(DataEditorService.class);

        Mock source = mock(InternalSource.class);
        String[] cols = { "col1", "col2", "col3" };
        source.stubs().method("getCols").will(returnValue(cols));

        TablePresenter p = new ViewableTablePresenter(null, "table", (InternalSource) source.proxy(),
                (NonEditablePageManagerView) view.proxy(), (DataAccessService) service.proxy());

        String rowFilter = "rowFilter";
        String sortOrder = "COL2";
        Page page = new Page();
        service.expects(once()).method("applyConstraints").with(new IsInstanceOf(DataAccessToken.class), eq(rowFilter),
                eq(sortOrder)).will(returnValue(page));
        view.expects(once()).method("display").with(same(page));

        p.doApplyConstraints(rowFilter, sortOrder);
    }

    public void testShouldRaiseExceptionIfInvalidColsAreSpecifiedInSortOrderOnApplyConstraints() {
        Mock view = mock(NonEditablePageManagerView.class);
        Mock service = mock(DataEditorService.class);

        Mock source = mock(InternalSource.class);
        String[] cols = { "col1", "col2", "col3" };
        source.stubs().method("getCols").will(returnValue(cols));

        TablePresenter p = new ViewableTablePresenter(null, "table", (InternalSource) source.proxy(),
                (NonEditablePageManagerView) view.proxy(), (DataAccessService) service.proxy());

        String sortOrder = "invalid-row";
        try {
            p.doApplyConstraints(null, sortOrder);
        } catch (EmfException e) {
            return;
        }

        fail("Should have raised an exception when Sort Order contains invalid cols");
    }
    
    public void testShouldRaiseExceptionIfOneOfColsInSortOrderIsInvalidOnApplyConstraints() {
        Mock view = mock(NonEditablePageManagerView.class);
        Mock service = mock(DataEditorService.class);
        
        Mock source = mock(InternalSource.class);
        String[] cols = { "col1", "col2", "col3" };
        source.stubs().method("getCols").will(returnValue(cols));
        
        TablePresenter p = new ViewableTablePresenter(null, "table", (InternalSource) source.proxy(),
                (NonEditablePageManagerView) view.proxy(), (DataAccessService) service.proxy());
        
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

        TablePresenter p = new ViewableTablePresenter(version, "table", null, null, (DataAccessService) services
                .proxy());

        assertEquals(28, p.totalRecords());
    }

    public void testShouldDisplayFirstPageOnFirstNextCall() throws Exception {
        Mock services = mock(DataEditorService.class);
        Page page = new Page();
        services.stubs().method("getPage").with(isA(DataAccessToken.class), eq(new Integer(1))).will(returnValue(page));
        services.stubs().method("getPageCount").with(isA(DataAccessToken.class)).will(returnValue(new Integer(20)));

        Mock view = mock(NonEditablePageManagerView.class);
        view.expects(once()).method("display").with(eq(page));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        TablePresenter p = new ViewableTablePresenter(new Version(), "table", null, (NonEditablePageManagerView) view
                .proxy(), (DataAccessService) services.proxy());

        p.doDisplayNext();
    }

    public void testShouldDisplaySpecifiedPage() throws Exception {
        Mock services = mock(DataEditorService.class);
        Page page = new Page();
        services.stubs().method("getPage").with(isA(DataAccessToken.class), eq(new Integer(21)))
                .will(returnValue(page));

        Mock view = mock(NonEditablePageManagerView.class);
        view.expects(once()).method("display").with(eq(page));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        TablePresenter p = new ViewableTablePresenter(new Version(), "table", null, (NonEditablePageManagerView) view
                .proxy(), (DataAccessService) services.proxy());

        p.doDisplay(21);
    }

    public void testShouldDisplayPageWithRecord() throws Exception {
        Mock service = mock(DataEditorService.class);
        Page page = new Page();
        service.stubs().method("getPageWithRecord").with(isA(DataAccessToken.class), eq(new Integer(21))).will(
                returnValue(page));

        Mock view = mock(NonEditablePageManagerView.class);
        view.expects(once()).method("display").with(eq(page));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        TablePresenter p = new ViewableTablePresenter(new Version(), "table", null, (NonEditablePageManagerView) view
                .proxy(), (DataAccessService) service.proxy());

        p.doDisplayPageWithRecord(21);
    }

    public void testShouldDisplayFirstPage() throws Exception {
        Mock services = mock(DataEditorService.class);
        Page page = new Page();
        services.stubs().method("getPage").with(isA(DataAccessToken.class), eq(new Integer(1))).will(returnValue(page));

        Mock view = mock(NonEditablePageManagerView.class);
        view.expects(once()).method("display").with(eq(page));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        TablePresenter p = new ViewableTablePresenter(new Version(), "table", null, (NonEditablePageManagerView) view
                .proxy(), (DataAccessService) services.proxy());

        p.doDisplayFirst();
    }

    public void testShouldDisplayFirstPageEvenAfterPrevRequestOnFirstPage() throws Exception {
        Mock services = mock(DataEditorService.class);
        Page page = new Page();
        page.setNumber(1);
        services.stubs().method("getPage").with(isA(DataAccessToken.class), eq(new Integer(1))).will(returnValue(page));

        Mock view = mock(NonEditablePageManagerView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        TablePresenter p = new ViewableTablePresenter(new Version(), "table", null, (NonEditablePageManagerView) view
                .proxy(), (DataAccessService) services.proxy());

        p.doDisplayFirst();
        p.doDisplayPrevious();
    }

    public void testShouldDisplayLastPageEvenAfterNextvRequestOnLastPage() throws Exception {
        Mock service = mock(DataEditorService.class);
        Page page = new Page();
        page.setNumber(20);
        service.stubs().method("getPage").with(isA(DataAccessToken.class), eq(new Integer(20))).will(returnValue(page));
        service.stubs().method("getPageCount").with(isA(DataAccessToken.class)).will(returnValue(new Integer(20)));

        Mock view = mock(NonEditablePageManagerView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page));
        view.stubs().method("scrollToPageEnd").withNoArguments();

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        TablePresenter p = new ViewableTablePresenter(new Version(), "table", null, (NonEditablePageManagerView) view
                .proxy(), (DataAccessService) service.proxy());

        p.doDisplayLast();
        p.doDisplayNext();
    }

    public void testShouldDisplayLastPage() throws Exception {
        Mock service = mock(DataEditorService.class);
        Page page = new Page();
        service.stubs().method("getPage").with(isA(DataAccessToken.class), eq(new Integer(20))).will(returnValue(page));
        service.stubs().method("getPageCount").with(isA(DataAccessToken.class)).will(returnValue(new Integer(20)));

        Mock view = mock(NonEditablePageManagerView.class);
        view.expects(once()).method("display").with(eq(page));
        view.stubs().method("scrollToPageEnd").withNoArguments();

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        TablePresenter p = new ViewableTablePresenter(new Version(), "table", null, (NonEditablePageManagerView) view
                .proxy(), (DataAccessService) service.proxy());

        p.doDisplayLast();
    }

    public void testShouldDisplaySecondPageOnTwoConsecutiveNextCall() throws Exception {
        Mock services = mock(DataEditorService.class);
        Page page = new Page();
        page.setNumber(1);
        services.expects(once()).method("getPage").with(isA(DataAccessToken.class), eq(new Integer(1))).will(
                returnValue(page));
        services.stubs().method("getPage").with(isA(DataAccessToken.class), eq(new Integer(2))).will(returnValue(page));
        services.stubs().method("getPageCount").with(isA(DataAccessToken.class)).will(returnValue(new Integer(20)));

        Mock view = mock(NonEditablePageManagerView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        TablePresenter p = new ViewableTablePresenter(new Version(), "table", null, (NonEditablePageManagerView) view
                .proxy(), (DataAccessService) services.proxy());

        p.doDisplayNext();
        p.doDisplayNext();
    }

    public void testShouldDisplayFirstPageOnDisplayPreviousAfterTwoConsecutiveNextCalls() throws Exception {
        Mock services = mock(DataEditorService.class);
        services.stubs().method("getPageCount").with(isA(DataAccessToken.class)).will(returnValue(new Integer(20)));

        Page page1 = new Page();
        page1.setNumber(1);
        services.expects(atLeastOnce()).method("getPage").with(isA(DataAccessToken.class), eq(new Integer(1))).will(
                returnValue(page1));
        Page page2 = new Page();
        page2.setNumber(2);
        services.expects(once()).method("getPage").with(isA(DataAccessToken.class), eq(new Integer(2))).will(
                returnValue(page2));

        Mock view = mock(NonEditablePageManagerView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page1));
        view.expects(once()).method("display").with(eq(page2));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getId").withNoArguments().will(returnValue(new Long(2)));

        TablePresenter p = new ViewableTablePresenter(new Version(), "table", null, (NonEditablePageManagerView) view
                .proxy(), (DataAccessService) services.proxy());

        p.doDisplayNext();
        p.doDisplayNext();
        p.doDisplayPrevious();
    }
}
