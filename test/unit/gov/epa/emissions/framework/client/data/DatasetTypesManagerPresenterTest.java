package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DatasetTypeService;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.ui.ViewLayout;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

//FIXME: this test looks way too complicated
public class DatasetTypesManagerPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        DatasetType[] types = { new DatasetType("name1"), new DatasetType("name2") };

        Mock service = mock(DatasetTypeService.class);
        service.stubs().method("getDatasetTypes").withNoArguments().will(returnValue(types));
        DatasetTypeService servicesProxy = (DatasetTypeService) service.proxy();

        Mock view = mock(DatasetTypesManagerView.class);
        view.expects(once()).method("display").with(same(servicesProxy));

        Mock locator = mock(ServiceLocator.class);
        locator.stubs().method("datasetTypeService").withNoArguments().will(returnValue(servicesProxy));

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

    public void testShouldDisplayEditableOnEdit() throws Exception {
        Mock view = mock(DatasetTypesManagerView.class);

        DatasetType type = new DatasetType();
        type.setName("name");

        Keyword[] keywords = new Keyword[0];
        Mock updateView = mock(EditableDatasetTypeView.class);
        updateView.expects(once()).method("observe").with(new IsInstanceOf(EditableDatasetTypePresenter.class));
        updateView.expects(once()).method("display").with(same(type), same(keywords));
        EditableDatasetTypeView updateProxy = (EditableDatasetTypeView) updateView.proxy();

        Mock layout = mock(ViewLayout.class);
        layout.expects(once()).method("add").with(eq(updateProxy), new IsInstanceOf(Object.class));
        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.FALSE));

        Mock locator = mock(ServiceLocator.class);
        locator.stubs().method("datasetTypeService").withNoArguments().will(returnValue(null));
        Mock interdataServices = mock(DataCommonsService.class);
        interdataServices.stubs().method("getKeywords").withNoArguments().will(returnValue(keywords));
        locator.stubs().method("dataCommonsService").withNoArguments().will(returnValue(interdataServices.proxy()));

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter((DatasetTypesManagerView) view.proxy(),
                (ServiceLocator) locator.proxy(), (ViewLayout) layout.proxy());

        p.doEdit(type, updateProxy);
    }
    
    public void testShouldShowViewableOnView() throws Exception {
        Mock view = mock(DatasetTypesManagerView.class);
        
        DatasetType type = new DatasetType();
        type.setName("name");
        
        Keyword[] keywords = new Keyword[0];
        Mock viewable = mock(ViewableDatasetTypeView.class);
        viewable.expects(once()).method("observe").with(new IsInstanceOf(ViewableDatasetTypePresenter.class));
        viewable.expects(once()).method("display").with(same(type), same(keywords));
        ViewableDatasetTypeView viewableProxy = (ViewableDatasetTypeView) viewable.proxy();
        
        Mock layout = mock(ViewLayout.class);
        layout.expects(once()).method("add").with(eq(viewableProxy), new IsInstanceOf(Object.class));
        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.FALSE));
        
        Mock locator = mock(ServiceLocator.class);
        locator.stubs().method("datasetTypeService").withNoArguments().will(returnValue(null));
        Mock interdataServices = mock(DataCommonsService.class);
        interdataServices.stubs().method("getKeywords").withNoArguments().will(returnValue(keywords));
        locator.stubs().method("dataCommonsService").withNoArguments().will(returnValue(interdataServices.proxy()));
        
        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter((DatasetTypesManagerView) view.proxy(),
                (ServiceLocator) locator.proxy(), (ViewLayout) layout.proxy());
        
        p.doView(type, viewableProxy);
    }

    public void testShouldActivateAlreadyDisplayedViewOnRepeatedUpdateOfSameView() throws Exception {
        Mock view = mock(DatasetTypesManagerView.class);

        DatasetType type = new DatasetType();
        type.setName("name");

        Keyword[] keywords = new Keyword[0];
        Mock updateView = mock(EditableDatasetTypeView.class);
        updateView.expects(once()).method("observe").with(new IsInstanceOf(EditableDatasetTypePresenter.class));
        updateView.expects(once()).method("display").with(same(type), same(keywords));
        EditableDatasetTypeView updateProxy = (EditableDatasetTypeView) updateView.proxy();

        Mock layout = mock(ViewLayout.class);
        layout.expects(once()).method("add").with(eq(updateProxy), new IsInstanceOf(Object.class));
        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.FALSE));

        Mock locator = mock(ServiceLocator.class);
        locator.stubs().method("datasetTypeService").withNoArguments().will(returnValue(null));

        Mock interdataServices = mock(DataCommonsService.class);
        interdataServices.stubs().method("getKeywords").withNoArguments().will(returnValue(keywords));
        locator.stubs().method("dataCommonsService").withNoArguments().will(returnValue(interdataServices.proxy()));

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter((DatasetTypesManagerView) view.proxy(),
                (ServiceLocator) locator.proxy(), (ViewLayout) layout.proxy());

        p.doEdit(type, updateProxy);

        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.TRUE));
        p.doEdit(type, updateProxy);
    }
}
