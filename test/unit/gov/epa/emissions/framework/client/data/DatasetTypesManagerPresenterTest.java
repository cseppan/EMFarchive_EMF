package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.ui.ViewLayout;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

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
                (DatasetTypesManagerView) view.proxy(), null);
        view.expects(once()).method("observe").with(eq(p));

        p.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        Mock view = mock(DatasetTypesManagerView.class);
        view.expects(once()).method("close").withNoArguments();

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter(null, (DatasetTypesManagerView) view.proxy(),
                null);

        p.doClose();
    }

    public void testShouldDisplayEditableOnEdit() throws Exception {
        DatasetType type = new DatasetType();

        Mock editable = mock(EditableDatasetTypeView.class);
        EditableDatasetTypeView editableProxy = (EditableDatasetTypeView) editable.proxy();

        Mock layout = mock(ViewLayout.class);
        layout.expects(once()).method("add").with(eq(editableProxy), new IsInstanceOf(Object.class));
        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.FALSE));

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter(null, null, (ViewLayout) layout.proxy());

        Mock presenter = mock(EditableDatasetTypePresenter.class);
        presenter.expects(once()).method("doDisplay").withNoArguments();

        p.edit(type, editableProxy, (EditableDatasetTypePresenter) presenter.proxy());
    }

    public void testShouldShowViewableOnView() throws Exception {
        DatasetType type = new DatasetType();
        type.setName("name");

        Mock viewable = mock(ViewableDatasetTypeView.class);
        ViewableDatasetTypeView viewableProxy = (ViewableDatasetTypeView) viewable.proxy();

        Mock layout = mock(ViewLayout.class);
        layout.expects(once()).method("add").with(eq(viewableProxy), new IsInstanceOf(Object.class));
        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.FALSE));

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter(null, null, (ViewLayout) layout.proxy());

        Mock presenter = mock(ViewableDatasetTypePresenter.class);
        presenter.expects(once()).method("doDisplay").withNoArguments();

        p.view(type, viewableProxy, (ViewableDatasetTypePresenter) presenter.proxy());
    }

    public void testShouldActivateAlreadyDisplayedViewOnRepeatedUpdateOfSameView() throws Exception {
        DatasetType type = new DatasetType();
        type.setName("name");

        Mock updateView = mock(EditableDatasetTypeView.class);
        EditableDatasetTypeView updateProxy = (EditableDatasetTypeView) updateView.proxy();

        Mock layout = mock(ViewLayout.class);
        layout.expects(once()).method("add").with(eq(updateProxy), new IsInstanceOf(Object.class));
        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.FALSE));

        DatasetTypesManagerPresenter p = new DatasetTypesManagerPresenter(null, null, (ViewLayout) layout.proxy());

        Mock presenter = mock(EditableDatasetTypePresenter.class);
        presenter.expects(once()).method("doDisplay").withNoArguments();
        EditableDatasetTypePresenter presenterProxy = (EditableDatasetTypePresenter) presenter.proxy();

        p.edit(type, updateProxy, presenterProxy);

        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.TRUE));
        p.edit(type, updateProxy, presenterProxy);
    }
}
