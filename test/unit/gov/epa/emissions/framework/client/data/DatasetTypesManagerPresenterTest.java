package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataCommonsService;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

//FIXME: this test looks way too complicated
public class DatasetTypesManagerPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        Mock service = mock(DataCommonsService.class);
        DataCommonsService serviceProxy = (DataCommonsService) service.proxy();

        Mock view = mock(DatasetTypesManagerView.class);
        view.expects(once()).method("display").with(same(serviceProxy));

        Mock locator = mock(ServiceLocator.class);
        locator.stubs().method("dataCommonsService").withNoArguments().will(returnValue(serviceProxy));

        Mock session = mock(EmfSession.class);
        session.stubs().method("serviceLocator").withNoArguments().will(returnValue(locator.proxy()));

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter((EmfSession) session.proxy(),
                (DatasetTypesManagerView) view.proxy());
        view.expects(once()).method("observe").with(eq(p));

        p.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        Mock view = mock(DatasetTypesManagerView.class);
        view.expects(once()).method("close").withNoArguments();

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter(null, (DatasetTypesManagerView) view.proxy());

        p.doClose();
    }

    public void testShouldDisplayEditableOnEdit() throws Exception {
        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter(null, null);

        Mock presenter = mock(EditableDatasetTypePresenter.class);
        presenter.expects(once()).method("doDisplay").withNoArguments();

        p.edit((EditableDatasetTypePresenter) presenter.proxy());
    }

    public void testShouldShowViewableOnView() throws Exception {
        DatasetType type = new DatasetType();
        type.setName("name");

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter(null, null);

        Mock presenter = mock(ViewableDatasetTypePresenter.class);
        presenter.expects(once()).method("doDisplay").withNoArguments();

        p.view((ViewableDatasetTypePresenter) presenter.proxy());
    }

}
