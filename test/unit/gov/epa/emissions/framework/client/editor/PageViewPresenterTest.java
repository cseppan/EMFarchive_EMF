package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.framework.services.DataEditorServices;
import gov.epa.emissions.framework.services.Page;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class PageViewPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayFirstPageOnFirstNextCall() throws Exception {
        Mock services = mock(DataEditorServices.class);
        Page page = new Page();
        services.stubs().method("getPage").with(eq("table"), eq(new Integer(1))).will(returnValue(page));

        Mock view = mock(PageView.class);
        view.expects(once()).method("display").with(eq(page));

        PageViewPresenter p = new PageViewPresenter((DataEditorServices) services.proxy(), (PageView) view.proxy(),
                "table");

        p.doDisplayNext();
    }

    public void testShouldDisplaySecondPageOnTwoConsecutiveNextCall() throws Exception {
        Mock services = mock(DataEditorServices.class);
        Page page = new Page();
        services.expects(once()).method("getPage").with(eq("table"), eq(new Integer(1))).will(returnValue(page));
        services.stubs().method("getPage").with(eq("table"), eq(new Integer(2))).will(returnValue(page));

        Mock view = mock(PageView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page));

        PageViewPresenter p = new PageViewPresenter((DataEditorServices) services.proxy(), (PageView) view.proxy(),
                "table");

        p.doDisplayNext();
        p.doDisplayNext();
    }

    public void testShouldDisplayFirstPageOnDisplayPreviousAfterTwoConsecutiveNextCalls() throws Exception {
        Mock services = mock(DataEditorServices.class);

        Page page1 = new Page();
        services.expects(atLeastOnce()).method("getPage").with(eq("table"), eq(new Integer(1)))
                .will(returnValue(page1));
        Page page2 = new Page();
        services.expects(once()).method("getPage").with(eq("table"), eq(new Integer(2))).will(returnValue(page2));

        Mock view = mock(PageView.class);
        view.expects(atLeastOnce()).method("display").with(eq(page1));
        view.expects(once()).method("display").with(eq(page2));

        PageViewPresenter p = new PageViewPresenter((DataEditorServices) services.proxy(), (PageView) view.proxy(),
                "table");

        p.doDisplayNext();
        p.doDisplayNext();
        p.doDisplayPrevious();
    }
}
