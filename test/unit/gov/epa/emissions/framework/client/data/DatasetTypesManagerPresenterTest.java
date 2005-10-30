package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.services.DatasetTypesServices;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class DatasetTypesManagerPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        DatasetType[] types = { new DatasetType("name1"), new DatasetType("name2") };

        Mock service = mock(DatasetTypesServices.class);
        service.stubs().method("getDatasetTypes").withNoArguments().will(returnValue(types));
        DatasetTypesServices servicesProxy = (DatasetTypesServices) service.proxy();

        Mock view = mock(DatasetTypesManagerView.class);
        view.expects(once()).method("display").with(same(servicesProxy));

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter((DatasetTypesManagerView) view.proxy(),
                servicesProxy);
        view.expects(once()).method("observe").with(eq(p));

        p.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        Mock view = mock(DatasetTypesManagerView.class);
        view.expects(once()).method("close").withNoArguments();

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter((DatasetTypesManagerView) view.proxy(), null);

        p.doClose();
    }

    public void testShouldDisplayUpdateViewOnUpdate() throws Exception {
        Mock view = mock(DatasetTypesManagerView.class);

        DatasetType type = new DatasetType();

        Mock updateView = mock(UpdateDatasetTypeView.class);
        updateView.expects(once()).method("observe").with(new IsInstanceOf(UpdateDatasetTypePresenter.class));
        updateView.expects(once()).method("display").with(same(type));

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter((DatasetTypesManagerView) view.proxy(), null);
        p.doUpdate(type, (UpdateDatasetTypeView) updateView.proxy());
    }
}
