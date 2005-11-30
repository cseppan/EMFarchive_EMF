package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.SimplePage;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class PageViewPresenterTest extends MockObjectTestCase {

    public void testShouldObserveOnObserveView() throws Exception {
        Mock services = mock(DataEditorService.class);
        Mock view = mock(PageView.class);

        PageViewPresenter p = new PageViewPresenter((DataEditorService) services.proxy(), (PageView) view.proxy(),
                "table");
        view.expects(once()).method("observe").with(eq(p));

        p.observeView();
    }

    public void testShouldFetchTotalRecords() throws Exception {
        Mock services = mock(DataEditorService.class);
        services.stubs().method("getTotalRecords").with(eq("table")).will(returnValue(new Integer(28)));

        Mock view = mock(PageView.class);

        PageViewPresenter p = new PageViewPresenter((DataEditorService) services.proxy(), (PageView) view.proxy(),
                "table");

        assertEquals(28, p.totalRecords());
    }

    public void testShouldDisplayFirstPageOnFirstNextCall() throws Exception {
        Mock services = mock(DataEditorService.class);
        SimplePage page = new SimplePage();
        services.stubs().method("getPage").with(eq("table"), eq(new Integer(1))).will(returnValue(page));
        services.stubs().method("getPageCount").with(eq("table")).will(returnValue(new Integer(20)));

        Mock view = mock(PageView.class);
        view.expects(once()).method("display").with(eq(page));

        PageViewPresenter p = new PageViewPresenter((DataEditorService) services.proxy(), (PageView) view.proxy(),
                "table");

        p.doDisplayNext();
    }

    public void testShouldDisplaySpecifiedPage() throws Exception {
        Mock services = mock(DataEditorService.class);
        SimplePage page = new SimplePage();
        services.stubs().method("getPage").with(eq("table"), eq(new Integer(21))).will(returnValue(page));

        Mock view = mock(PageView.class);
        view.expects(once()).method("display").with(eq(page));

        PageViewPresenter p = new PageViewPresenter((DataEditorService) services.proxy(), (PageView) view.proxy(),
                "table");

        p.doDisplay(21);
    }

    public void testShouldDisplayPageWithRecord() throws Exception {
        Mock services = mock(DataEditorService.class);
        SimplePage page = new SimplePage();
        services.stubs().method("getPageWithRecord").with(eq("table"), eq(new Integer(21))).will(returnValue(page));

        Mock view = mock(PageView.class);
        view.expects(once()).method("display").with(eq(page));

        PageViewPresenter p = new PageViewPresenter((DataEditorService) services.proxy(), (PageView) view.proxy(),
                "table");

        p.doDisplayPageWithRecord(21);
    }

    public void testShouldDisplayFirstPage() throws Exception {
        Mock services = mock(DataEditorService.class);
        SimplePage page = new SimplePage();
        services.stubs().method("getPage").with(eq("table"), eq(new Integer(1))).will(returnValue(page));

        Mock view = mock(PageView.class);
        view.expects(once()).method("display").with(eq(page));

        PageViewPresenter p = new PageViewPresenter((DataEditorService) services.proxy(), (PageView) view.proxy(),
                "table");

        p.doDisplayFirst();
    }

    public void testShouldDisplayFirstPageEvenAfterPrevRequestOnFirstPage() throws Exception {
        Mock services = mock(DataEditorService.class);
        SimplePage page = new SimplePage();
        services.stubs().method("getPage").with(eq("table"), eq(new Integer(1))).will(returnValue(page));

        Mock view = mock(PageView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page));

        PageViewPresenter p = new PageViewPresenter((DataEditorService) services.proxy(), (PageView) view.proxy(),
                "table");

        p.doDisplayFirst();
        p.doDisplayPrevious();
    }

    public void testShouldDisplayLastPageEvenAfterNextvRequestOnLastPage() throws Exception {
        Mock services = mock(DataEditorService.class);
        SimplePage page = new SimplePage();
        services.stubs().method("getPage").with(eq("table"), eq(new Integer(20))).will(returnValue(page));
        services.stubs().method("getPageCount").with(eq("table")).will(returnValue(new Integer(20)));

        Mock view = mock(PageView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page));

        PageViewPresenter p = new PageViewPresenter((DataEditorService) services.proxy(), (PageView) view.proxy(),
                "table");

        p.doDisplayLast();
        p.doDisplayNext();
    }

    public void testShouldDisplayLastPage() throws Exception {
        Mock services = mock(DataEditorService.class);
        SimplePage page = new SimplePage();
        services.stubs().method("getPage").with(eq("table"), eq(new Integer(20))).will(returnValue(page));
        services.stubs().method("getPageCount").with(eq("table")).will(returnValue(new Integer(20)));

        Mock view = mock(PageView.class);
        view.expects(once()).method("display").with(eq(page));

        PageViewPresenter p = new PageViewPresenter((DataEditorService) services.proxy(), (PageView) view.proxy(),
                "table");

        p.doDisplayLast();
    }

    public void testShouldDisplaySecondPageOnTwoConsecutiveNextCall() throws Exception {
        Mock services = mock(DataEditorService.class);
        SimplePage page = new SimplePage();
        services.expects(once()).method("getPage").with(eq("table"), eq(new Integer(1))).will(returnValue(page));
        services.stubs().method("getPage").with(eq("table"), eq(new Integer(2))).will(returnValue(page));
        services.stubs().method("getPageCount").with(eq("table")).will(returnValue(new Integer(20)));

        Mock view = mock(PageView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page));

        PageViewPresenter p = new PageViewPresenter((DataEditorService) services.proxy(), (PageView) view.proxy(),
                "table");

        p.doDisplayNext();
        p.doDisplayNext();
    }

    public void testShouldDisplayFirstPageOnDisplayPreviousAfterTwoConsecutiveNextCalls() throws Exception {
        Mock services = mock(DataEditorService.class);
        services.stubs().method("getPageCount").with(eq("table")).will(returnValue(new Integer(20)));

        SimplePage page1 = new SimplePage();
        services.expects(atLeastOnce()).method("getPage").with(eq("table"), eq(new Integer(1)))
                .will(returnValue(page1));
        SimplePage page2 = new SimplePage();
        services.expects(once()).method("getPage").with(eq("table"), eq(new Integer(2))).will(returnValue(page2));

        Mock view = mock(PageView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page1));
        view.expects(once()).method("display").with(eq(page2));

        PageViewPresenter p = new PageViewPresenter((DataEditorService) services.proxy(), (PageView) view.proxy(),
                "table");

        p.doDisplayNext();
        p.doDisplayNext();
        p.doDisplayPrevious();
    }
}
