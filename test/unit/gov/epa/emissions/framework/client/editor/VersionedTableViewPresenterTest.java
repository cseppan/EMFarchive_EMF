package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.framework.client.meta.VersionedTablePresenter;
import gov.epa.emissions.framework.client.meta.VersionedTableView;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EditToken;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class VersionedTableViewPresenterTest extends MockObjectTestCase {

    public void testShouldObserveOnObserveView() throws Exception {
        Mock services = mock(DataEditorService.class);
        Mock view = mock(VersionedTableView.class);

        VersionedTablePresenter p = new VersionedTablePresenter(new Version(), "table", (VersionedTableView) view
                .proxy(), (DataEditorService) services.proxy());
        view.expects(once()).method("observe").with(eq(p));

        p.observeView();
    }

    public void testShouldFetchTotalRecords() throws Exception {
        Mock services = mock(DataEditorService.class);
        Version version = new Version();
        services.stubs().method("getTotalRecords").with(isA(EditToken.class)).will(returnValue(new Integer(28)));

        VersionedTablePresenter p = new VersionedTablePresenter(version, "table", null, (DataEditorService) services
                .proxy());

        assertEquals(28, p.totalRecords());
    }

    public void testShouldDisplayFirstPageOnFirstNextCall() throws Exception {
        Mock services = mock(DataEditorService.class);
        Page page = new Page();
        services.stubs().method("getPage").with(isA(EditToken.class), eq(new Integer(1))).will(returnValue(page));
        services.stubs().method("getPageCount").with(isA(EditToken.class)).will(returnValue(new Integer(20)));

        Mock view = mock(VersionedTableView.class);
        view.expects(once()).method("display").with(eq(page));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getDatasetid").withNoArguments().will(returnValue(new Long(2)));

        VersionedTablePresenter p = new VersionedTablePresenter(new Version(), "table", (VersionedTableView) view
                .proxy(), (DataEditorService) services.proxy());

        p.doDisplayNext();
    }

    public void testShouldDisplaySpecifiedPage() throws Exception {
        Mock services = mock(DataEditorService.class);
        Page page = new Page();
        services.stubs().method("getPage").with(isA(EditToken.class), eq(new Integer(21))).will(returnValue(page));

        Mock view = mock(VersionedTableView.class);
        view.expects(once()).method("display").with(eq(page));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getDatasetid").withNoArguments().will(returnValue(new Long(2)));

        VersionedTablePresenter p = new VersionedTablePresenter(new Version(), "table", (VersionedTableView) view
                .proxy(), (DataEditorService) services.proxy());

        p.doDisplay(21);
    }

    public void testShouldDisplayPageWithRecord() throws Exception {
        Mock services = mock(DataEditorService.class);
        Page page = new Page();
        services.stubs().method("getPageWithRecord").with(isA(EditToken.class), eq(new Integer(21))).will(
                returnValue(page));

        Mock view = mock(VersionedTableView.class);
        view.expects(once()).method("display").with(eq(page));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getDatasetid").withNoArguments().will(returnValue(new Long(2)));

        VersionedTablePresenter p = new VersionedTablePresenter(new Version(), "table", (VersionedTableView) view
                .proxy(), (DataEditorService) services.proxy());

        p.doDisplayPageWithRecord(21);
    }

    public void testShouldDisplayFirstPage() throws Exception {
        Mock services = mock(DataEditorService.class);
        Page page = new Page();
        services.stubs().method("getPage").with(isA(EditToken.class), eq(new Integer(1))).will(returnValue(page));

        Mock view = mock(VersionedTableView.class);
        view.expects(once()).method("display").with(eq(page));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getDatasetid").withNoArguments().will(returnValue(new Long(2)));

        VersionedTablePresenter p = new VersionedTablePresenter(new Version(), "table", (VersionedTableView) view
                .proxy(), (DataEditorService) services.proxy());

        p.doDisplayFirst();
    }

    public void testShouldDisplayFirstPageEvenAfterPrevRequestOnFirstPage() throws Exception {
        Mock services = mock(DataEditorService.class);
        Page page = new Page();
        services.stubs().method("getPage").with(isA(EditToken.class), eq(new Integer(1))).will(returnValue(page));

        Mock view = mock(VersionedTableView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getDatasetid").withNoArguments().will(returnValue(new Long(2)));

        VersionedTablePresenter p = new VersionedTablePresenter(new Version(), "table", (VersionedTableView) view
                .proxy(), (DataEditorService) services.proxy());

        p.doDisplayFirst();
        p.doDisplayPrevious();
    }

    public void testShouldDisplayLastPageEvenAfterNextvRequestOnLastPage() throws Exception {
        Mock services = mock(DataEditorService.class);
        Page page = new Page();
        services.stubs().method("getPage").with(isA(EditToken.class), eq(new Integer(20))).will(returnValue(page));
        services.stubs().method("getPageCount").with(isA(EditToken.class)).will(returnValue(new Integer(20)));

        Mock view = mock(VersionedTableView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getDatasetid").withNoArguments().will(returnValue(new Long(2)));

        VersionedTablePresenter p = new VersionedTablePresenter(new Version(), "table", (VersionedTableView) view
                .proxy(), (DataEditorService) services.proxy());

        p.doDisplayLast();
        p.doDisplayNext();
    }

    public void testShouldDisplayLastPage() throws Exception {
        Mock services = mock(DataEditorService.class);
        Page page = new Page();
        services.stubs().method("getPage").with(isA(EditToken.class), eq(new Integer(20))).will(returnValue(page));
        services.stubs().method("getPageCount").with(isA(EditToken.class)).will(returnValue(new Integer(20)));

        Mock view = mock(VersionedTableView.class);
        view.expects(once()).method("display").with(eq(page));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getDatasetid").withNoArguments().will(returnValue(new Long(2)));

        VersionedTablePresenter p = new VersionedTablePresenter(new Version(), "table", (VersionedTableView) view
                .proxy(), (DataEditorService) services.proxy());

        p.doDisplayLast();
    }

    public void testShouldDisplaySecondPageOnTwoConsecutiveNextCall() throws Exception {
        Mock services = mock(DataEditorService.class);
        Page page = new Page();
        services.expects(once()).method("getPage").with(isA(EditToken.class), eq(new Integer(1))).will(
                returnValue(page));
        services.stubs().method("getPage").with(isA(EditToken.class), eq(new Integer(2))).will(returnValue(page));
        services.stubs().method("getPageCount").with(isA(EditToken.class)).will(returnValue(new Integer(20)));

        Mock view = mock(VersionedTableView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getDatasetid").withNoArguments().will(returnValue(new Long(2)));

        VersionedTablePresenter p = new VersionedTablePresenter(new Version(), "table", (VersionedTableView) view
                .proxy(), (DataEditorService) services.proxy());

        p.doDisplayNext();
        p.doDisplayNext();
    }

    public void testShouldDisplayFirstPageOnDisplayPreviousAfterTwoConsecutiveNextCalls() throws Exception {
        Mock services = mock(DataEditorService.class);
        services.stubs().method("getPageCount").with(isA(EditToken.class)).will(returnValue(new Integer(20)));

        Page page1 = new Page();
        services.expects(atLeastOnce()).method("getPage").with(isA(EditToken.class), eq(new Integer(1))).will(
                returnValue(page1));
        Page page2 = new Page();
        services.expects(once()).method("getPage").with(isA(EditToken.class), eq(new Integer(2))).will(
                returnValue(page2));

        Mock view = mock(VersionedTableView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page1));
        view.expects(once()).method("display").with(eq(page2));

        Mock dataset = mock(Dataset.class);
        dataset.stubs().method("getDatasetid").withNoArguments().will(returnValue(new Long(2)));

        VersionedTablePresenter p = new VersionedTablePresenter(new Version(), "table", (VersionedTableView) view
                .proxy(), (DataEditorService) services.proxy());

        p.doDisplayNext();
        p.doDisplayNext();
        p.doDisplayPrevious();
    }
}
