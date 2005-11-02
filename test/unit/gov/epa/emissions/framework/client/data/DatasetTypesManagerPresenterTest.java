package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DatasetTypesServices;
import gov.epa.emissions.framework.services.InterDataServices;
import gov.epa.emissions.framework.ui.ViewLayout;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

//FIXME: this test looks way too complicated
public class DatasetTypesManagerPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        DatasetType[] types = { new DatasetType("name1"), new DatasetType("name2") };

        Mock service = mock(DatasetTypesServices.class);
        service.stubs().method("getDatasetTypes").withNoArguments().will(returnValue(types));
        DatasetTypesServices servicesProxy = (DatasetTypesServices) service.proxy();

        Mock view = mock(DatasetTypesManagerView.class);
        view.expects(once()).method("display").with(same(servicesProxy));

        Mock locator = mock(ServiceLocator.class);
        locator.stubs().method("getDatasetTypesServices").withNoArguments().will(returnValue(servicesProxy));

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter((DatasetTypesManagerView) view.proxy(),
                (ServiceLocator) locator.proxy(), null);
        view.expects(once()).method("observe").with(eq(p));

        p.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        Mock view = mock(DatasetTypesManagerView.class);
        view.expects(once()).method("close").withNoArguments();

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter((DatasetTypesManagerView) view.proxy(), null,
                null);

        p.doClose();
    }

    public void testShouldDisplayUpdateViewOnUpdate() throws Exception {
        Mock view = mock(DatasetTypesManagerView.class);

        DatasetType type = new DatasetType();
        type.setName("name");

        Keyword[] keywords = new Keyword[0];
        Mock updateView = mock(UpdateDatasetTypeView.class);
        updateView.expects(once()).method("observe").with(new IsInstanceOf(UpdateDatasetTypePresenter.class));
        updateView.expects(once()).method("display").with(same(type), same(keywords));
        UpdateDatasetTypeView updateProxy = (UpdateDatasetTypeView) updateView.proxy();

        Mock layout = mock(ViewLayout.class);
        layout.expects(once()).method("add").with(eq(updateProxy), new IsInstanceOf(Object.class));
        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.FALSE));

        Mock locator = mock(ServiceLocator.class);
        locator.stubs().method("getDatasetTypesServices").withNoArguments().will(returnValue(null));
        Mock interdataServices = mock(InterDataServices.class);
        interdataServices.stubs().method("getKeywords").withNoArguments().will(returnValue(keywords));
        locator.stubs().method("getInterDataServices").withNoArguments().will(returnValue(interdataServices.proxy()));

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter((DatasetTypesManagerView) view.proxy(),
                (ServiceLocator) locator.proxy(), (ViewLayout) layout.proxy());

        p.doUpdate(type, updateProxy);
    }

    public void testShouldActivateAlreadyDisplayedViewOnRepeatedUpdateOfSameView() throws Exception {
        Mock view = mock(DatasetTypesManagerView.class);

        DatasetType type = new DatasetType();
        type.setName("name");

        Keyword[] keywords = new Keyword[0];
        Mock updateView = mock(UpdateDatasetTypeView.class);
        updateView.expects(once()).method("observe").with(new IsInstanceOf(UpdateDatasetTypePresenter.class));
        updateView.expects(once()).method("display").with(same(type), same(keywords));
        UpdateDatasetTypeView updateProxy = (UpdateDatasetTypeView) updateView.proxy();

        Mock layout = mock(ViewLayout.class);
        layout.expects(once()).method("add").with(eq(updateProxy), new IsInstanceOf(Object.class));
        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.FALSE));

        Mock locator = mock(ServiceLocator.class);
        locator.stubs().method("getDatasetTypesServices").withNoArguments().will(returnValue(null));

        Mock interdataServices = mock(InterDataServices.class);
        interdataServices.stubs().method("getKeywords").withNoArguments().will(returnValue(keywords));
        locator.stubs().method("getInterDataServices").withNoArguments().will(returnValue(interdataServices.proxy()));

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter((DatasetTypesManagerView) view.proxy(),
                (ServiceLocator) locator.proxy(), (ViewLayout) layout.proxy());

        p.doUpdate(type, updateProxy);

        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.TRUE));
        p.doUpdate(type, updateProxy);
    }
}
