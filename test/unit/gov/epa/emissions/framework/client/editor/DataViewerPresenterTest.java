package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.framework.services.DataEditorServices;
import gov.epa.emissions.framework.services.Page;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class DataViewerPresenterTest extends MockObjectTestCase {

    public void testShouldLoadTablesOfDatasetOnDisplay() throws Exception {
        Mock dataset = mock(Dataset.class);
        Dataset datasetProxy = (Dataset) dataset.proxy();

        Mock view = mock(DataView.class);
        view.expects(once()).method("display").with(eq(datasetProxy));

        DataViewPresenter p = new DataViewPresenter(datasetProxy, (DataView) view.proxy());
        view.expects(once()).method("observe").with(same(p));

        p.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        Mock dataset = mock(Dataset.class);
        Dataset datasetProxy = (Dataset) dataset.proxy();

        Mock view = mock(DataView.class);
        view.expects(once()).method("close").withNoArguments();

        DataViewPresenter p = new DataViewPresenter(datasetProxy, (DataView) view.proxy());

        p.doClose();
    }

    public void testShouldLoadFirstPageOnTableSelection() throws Exception {
        Mock dataset = mock(Dataset.class);
        DataViewPresenter p = new DataViewPresenter(((Dataset) dataset.proxy()), null);

        Mock services = mock(DataEditorServices.class);
        Page page = new Page();
        services.stubs().method("getPage").with(eq("table"), eq(new Integer(1))).will(returnValue(page));
        services.stubs().method("getPageCount").with(eq("table")).will(returnValue(new Integer(10)));

        Mock pageView = mock(PageView.class);
        pageView.expects(once()).method("display").with(eq(page));

        p.doSelectTable("table", (PageView) pageView.proxy(), (DataEditorServices) services.proxy());
    }

}
