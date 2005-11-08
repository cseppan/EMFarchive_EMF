package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.framework.services.DataEditorServices;
import gov.epa.emissions.framework.services.Page;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class DataViewerPresenterTest extends MockObjectTestCase {

    public void testShouldFetchFirstPageOnDisplay() {
        Mock services = mock(DataEditorServices.class);
        Page page = new Page();
        services.stubs().method("getPage").with(eq("table"), eq(new Integer(2))).will(returnValue(page));

        Mock view = mock(DataView.class);
//        view.expects(once()).method("display").with(eq(page));
        
        DataViewPresenter p = new DataViewPresenter((DataEditorServices) services.proxy(), (DataView) view.proxy());

        p.doDisplay("table");
    }
}
