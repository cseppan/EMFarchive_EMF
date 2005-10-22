package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.services.DataServices;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class DatasetTypesManagerPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        DatasetType[] types = { new DatasetType("name1"), new DatasetType("name2") };

        Mock view = mock(DatasetTypesManagerView.class);
        view.expects(once()).method("display").with(eq(types));

        Mock service = mock(DataServices.class);
        service.stubs().method("getDatasetTypes").withNoArguments().will(returnValue(types));

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter((DatasetTypesManagerView) view.proxy(),
                (DataServices) service.proxy());
        view.expects(once()).method("observe").with(eq(p));

        p.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        Mock view = mock(DatasetTypesManagerView.class);
        view.expects(once()).method("close").withNoArguments();

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter((DatasetTypesManagerView) view.proxy(), null);

        p.doClose();
    }
}
