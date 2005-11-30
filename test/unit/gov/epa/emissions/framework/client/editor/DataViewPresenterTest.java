package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.SimplePage;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;
import org.jmock.core.matcher.InvokeCountMatcher;

public class DataViewPresenterTest extends MockObjectTestCase {

    public void testShouldLoadTablesOfDatasetOnDisplay() throws Exception {
        Mock dataset = mock(Dataset.class);
        Dataset datasetProxy = (Dataset) dataset.proxy();

        Mock view = mock(DataView.class);
        view.expects(once()).method("display").with(eq(datasetProxy));

        DataViewPresenter p = new DataViewPresenter(datasetProxy, (DataView) view.proxy(), null);
        view.expects(once()).method("observe").with(same(p));

        p.doDisplay();
    }

    public void testShouldCloseViewAndCloseDataEditSessionOnClose() throws Exception {
        Mock dataset = mock(Dataset.class);
        Dataset datasetProxy = (Dataset) dataset.proxy();

        Mock view = mock(DataView.class);
        view.expects(once()).method("close").withNoArguments();

        Mock services = mock(DataEditorService.class);
        services.expects(once()).method("close").withNoArguments();

        DataViewPresenter p = new DataViewPresenter(datasetProxy, (DataView) view.proxy(),
                (DataEditorService) services.proxy());

        p.doClose();
    }

    public void testShouldLoadFirstPageOnTableSelection() throws Exception {
        Mock dataset = mock(Dataset.class);

        Mock services = mock(DataEditorService.class);
        SimplePage page = new SimplePage();
        services.stubs().method("getPage").with(eq("table"), eq(new Integer(1))).will(returnValue(page));

        Mock pageView = mock(PageView.class);
        pageView.expects(once()).method("display").with(eq(page));
        pageView.expects(once()).method("observe").with(new IsInstanceOf(PageViewPresenter.class));

        DataViewPresenter presenter = new DataViewPresenter(((Dataset) dataset.proxy()), null,
                (DataEditorService) services.proxy());
        presenter.doSelectTable("table", (PageView) pageView.proxy());
    }

    public void testShouldBeAbleToDisplayMultipleTablesSimultaneously() throws Exception {
        Mock dataset = mock(Dataset.class);

        Mock services = mock(DataEditorService.class);
        SimplePage page1 = new SimplePage();
        SimplePage page2 = new SimplePage();
        services.expects(once()).method("getPage").with(eq("table1"), eq(new Integer(1))).will(returnValue(page1));
        services.expects(once()).method("getPage").with(eq("table2"), eq(new Integer(1))).will(returnValue(page2));

        Mock pageView = mock(PageView.class);
        pageView.expects(once()).method("display").with(eq(page1));
        pageView.expects(once()).method("display").with(eq(page2));
        pageView.expects(new InvokeCountMatcher(2)).method("observe").with(new IsInstanceOf(PageViewPresenter.class));

        DataViewPresenter p = new DataViewPresenter(((Dataset) dataset.proxy()), null, (DataEditorService) services
                .proxy());
        p.doSelectTable("table1", (PageView) pageView.proxy());
        p.doSelectTable("table2", (PageView) pageView.proxy());
    }

}
