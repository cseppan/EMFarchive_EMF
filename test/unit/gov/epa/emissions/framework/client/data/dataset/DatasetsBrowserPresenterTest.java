package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.data.dataset.DatasetsBrowserPresenter;
import gov.epa.emissions.framework.client.data.dataset.DatasetsBrowserView;
import gov.epa.emissions.framework.client.exim.ExportPresenter;
import gov.epa.emissions.framework.client.exim.ExportView;
import gov.epa.emissions.framework.client.exim.ImportPresenter;
import gov.epa.emissions.framework.client.exim.ImportPresenterStub;
import gov.epa.emissions.framework.client.exim.ImportView;
import gov.epa.emissions.framework.client.meta.PropertiesEditorPresenter;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.client.meta.versions.EditVersionsPresenter;
import gov.epa.emissions.framework.client.meta.versions.VersionedDataPresenter;
import gov.epa.emissions.framework.client.meta.versions.VersionedDataView;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.editor.DataViewService;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class DatasetsBrowserPresenterTest extends MockObjectTestCase {

    private Mock view;

    private DatasetsBrowserPresenter presenter;

    private Mock dataService;

    private Mock serviceLocator;

    private Mock session;

    protected void setUp() {
        view = mock(DatasetsBrowserView.class);

        dataService = mock(DataService.class);
        Mock dataCommonsService = mock(DataCommonsService.class);
        serviceLocator = mock(ServiceLocator.class);
        serviceLocator.stubs().method("dataService").withNoArguments().will(returnValue(dataService.proxy()));
        serviceLocator.stubs().method("dataCommonsService").withNoArguments().will(
                returnValue(dataCommonsService.proxy()));

        session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(new User()));

        presenter = new DatasetsBrowserPresenter((EmfSession) session.proxy());

        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").withNoArguments();

        presenter.doDisplay((DatasetsBrowserView) view.proxy());
    }

    public void testShouldCloseViewOnClickOfCloseButton() {
        view.expects(once()).method("disposeView").withNoArguments();

        presenter.doClose();
    }

    public void testShouldRefreshViewOnClickOfRefreshButton() throws EmfException {
        EmfDataset[] datasets = new EmfDataset[0];
        dataService.stubs().method("getDatasets").withNoArguments().will(returnValue(datasets));

        view.expects(once()).method("refresh").with(eq(datasets));

        Mock session = mock(EmfSession.class);
        session.stubs().method("dataService").will(returnValue(dataService.proxy()));

        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter((EmfSession) session.proxy());
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").withNoArguments();
        view.expects(once()).method("clearMessage").withNoArguments();

        presenter.doDisplay((DatasetsBrowserView) view.proxy());

        presenter.doRefresh();
    }

    public void testShouldDisplayExportViewOnClickOfExportButton() {
        EmfDataset dataset1 = new EmfDataset();
        dataset1.setName("name 1");
        EmfDataset[] datasets = new EmfDataset[] { dataset1 };

        view.expects(once()).method("clearMessage").withNoArguments();

        Mock exportView = mock(ExportView.class);
        Mock exportPresenter = mock(ExportPresenter.class);
        ExportView exportViewProxy = (ExportView) exportView.proxy();
        exportPresenter.expects(once()).method("display").with(eq(exportViewProxy));

        presenter.doExport(exportViewProxy, (ExportPresenter) exportPresenter.proxy(), datasets);
    }

    public void testShouldDisplayImportViewOnClickOfNewButton() {
        view.expects(once()).method("clearMessage").withNoArguments();

        Mock importView = mock(ImportView.class);
        Mock importPresenter = mock(ImportPresenterStub.class);
        ImportView importViewProxy = (ImportView) importView.proxy();
        importPresenter.expects(once()).method("display").with(eq(importViewProxy));

        presenter.doImport(importViewProxy, (ImportPresenter) importPresenter.proxy());
    }

    public void testShouldDisplayInformationalMessageOnClickOfExportButtonIfNoDatasetsAreSelected() {
        EmfDataset[] datasets = new EmfDataset[0];
        String message = "To Export, you will need to select at least one non-External type Dataset";
        view.expects(once()).method("showMessage").with(eq(message));

        presenter.doExport(null, null, datasets);
    }

    public void testShouldDisplayPropertiesEditorOnSelectionOfEditPropertiesOption() throws Exception {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("name");

        view.expects(once()).method("clearMessage").withNoArguments();

        Mock editorPresenter = mock(PropertiesEditorPresenter.class);
        editorPresenter.expects(once()).method("doDisplay");

        presenter.doDisplayPropertiesEditor((PropertiesEditorPresenter) editorPresenter.proxy());
    }

    public void testShouldDisplayPropertiesViewerOnSelectionOfViewPropertiesOption() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("name");

        view.expects(once()).method("clearMessage").withNoArguments();

        Mock propsView = mock(PropertiesView.class);
        propsView.expects(once()).method("observe").with(new IsInstanceOf(PropertiesViewPresenter.class));
        propsView.expects(once()).method("display").with(eq(dataset));

        PropertiesView viewProxy = (PropertiesView) propsView.proxy();

        presenter.doDisplayPropertiesView(viewProxy, dataset);
    }

    public void testShouldDisplayVersionsEditorOnSelectionOfEditDataOption() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("name");

        view.expects(once()).method("clearMessage").withNoArguments();

        Mock editorService = mock(DataEditorService.class);
        Mock viewService = mock(DataViewService.class);
        Object viewServiceProxy = viewService.proxy();

        Mock editorView = mock(VersionedDataView.class);
        editorView.expects(once()).method("observe").with(new IsInstanceOf(VersionedDataPresenter.class));
        editorView.expects(once()).method("display").with(eq(dataset), new IsInstanceOf(EditVersionsPresenter.class));

        session.stubs().method("serviceLocator").will(returnValue(serviceLocator.proxy()));
        session.stubs().method("dataViewService").will(returnValue(viewServiceProxy));
        session.stubs().method("dataEditorService").will(returnValue(editorService.proxy()));
        
        VersionedDataView viewProxy = (VersionedDataView) editorView.proxy();

        presenter.doDisplayVersionedData(viewProxy, dataset);
    }

}
