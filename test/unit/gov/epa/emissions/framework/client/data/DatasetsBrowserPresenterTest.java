package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.exim.ExportPresenter;
import gov.epa.emissions.framework.client.exim.ExportView;
import gov.epa.emissions.framework.client.exim.ImportPresenter;
import gov.epa.emissions.framework.client.exim.ImportPresenterStub;
import gov.epa.emissions.framework.client.exim.ImportView;
import gov.epa.emissions.framework.client.meta.PropertiesEditorPresenter;
import gov.epa.emissions.framework.client.meta.PropertiesEditorView;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.ui.WindowLayoutManager;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class DatasetsBrowserPresenterTest extends MockObjectTestCase {

    private Mock view;

    private DatasetsBrowserPresenter presenter;

    private Mock layout;

    private Mock dataServices;

    protected void setUp() {
        view = mock(DatasetsBrowserView.class);

        layout = mock(WindowLayoutManager.class);
        dataServices = mock(DataServices.class);
        presenter = new DatasetsBrowserPresenter((DataServices) dataServices.proxy(), (WindowLayoutManager) layout
                .proxy());

        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").withNoArguments();

        presenter.doDisplay((DatasetsBrowserView) view.proxy());
    }

    public void testShouldCloseViewOnClickOfCloseButton() {
        view.expects(once()).method("close").withNoArguments();

        presenter.doClose();
    }

    public void testShouldRefreshViewOnClickOfRefreshButton() throws EmfException {
        EmfDataset[] datasets = new EmfDataset[0];
        dataServices.stubs().method("getDatasets").withNoArguments().will(returnValue(datasets));

        view.expects(once()).method("refresh").with(eq(datasets));

        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter((DataServices) dataServices.proxy(), null);
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

        layout.expects(once()).method("add").with(eq(exportViewProxy), new IsInstanceOf(String.class));

        presenter.doExport(exportViewProxy, (ExportPresenter) exportPresenter.proxy(), datasets);
    }

    public void testShouldDisplayImportViewOnClickOfNewButton() throws EmfException {
        view.expects(once()).method("clearMessage").withNoArguments();

        Mock importView = mock(ImportView.class);
        Mock importPresenter = mock(ImportPresenterStub.class);
        ImportView importViewProxy = (ImportView) importView.proxy();
        importPresenter.expects(once()).method("display").with(eq(importViewProxy));

        layout.expects(once()).method("add").with(eq(importViewProxy), new IsInstanceOf(String.class));

        presenter.doNew(importViewProxy, (ImportPresenter) importPresenter.proxy());
    }

    public void testShouldDisplayInformationalMessageOnClickOfExportButtonIfNoDatasetsAreSelected() {
        EmfDataset[] datasets = new EmfDataset[0];
        String message = "To Export, you will need to select at least one Dataset";
        view.expects(once()).method("showMessage").with(eq(message));

        presenter.doExport(null, null, datasets);
    }

    public void testShouldDisplayPropertiesEditorOnClickOfPropertiesButton() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("name");

        view.expects(once()).method("clearMessage").withNoArguments();

        Mock metadataView = mock(PropertiesEditorView.class);
        metadataView.expects(once()).method("observe").with(new IsInstanceOf(PropertiesEditorPresenter.class));
        metadataView.expects(once()).method("display").with(eq(dataset));

        PropertiesEditorView viewProxy = (PropertiesEditorView) metadataView.proxy();
        layout.expects(once()).method("add").with(eq(viewProxy), new IsInstanceOf(String.class));

        presenter.doShowProperties(viewProxy, dataset);
    }

    public void testShouldDisplayTheSamePropertiesEditorAsPreviouslyDisplayedOnSelectingTheSameDatasetAndClickingProperties() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("name");

        view.expects(atLeastOnce()).method("clearMessage").withNoArguments();

        Mock propertiesEditorView = mock(PropertiesEditorView.class);
        propertiesEditorView.expects(once()).method("observe").with(new IsInstanceOf(PropertiesEditorPresenter.class));
        propertiesEditorView.expects(once()).method("display").with(eq(dataset));

        PropertiesEditorView viewProxy = (PropertiesEditorView) propertiesEditorView.proxy();
        layout.expects(once()).method("add").with(eq(viewProxy), new IsInstanceOf(String.class));

        // 1st display
        presenter.doShowProperties(viewProxy, dataset);

        // 2nd attempt
        propertiesEditorView.stubs().method("isAlive").withNoArguments().will(returnValue(Boolean.TRUE));
        propertiesEditorView.expects(once()).method("bringToFront").withNoArguments();
        presenter.doShowProperties(viewProxy, dataset);
    }

    public void testShouldDisplayNewPropertiesEditorIfPreviouslyOpenedEditorIsClosedOnClickingOfPropertiesButton() {
        view.expects(atLeastOnce()).method("clearMessage").withNoArguments();

        // 1st attempt
        EmfDataset dataset = new EmfDataset();
        dataset.setName("name");

        Mock view1 = mock(PropertiesEditorView.class);
        view1.expects(once()).method("observe").with(new IsInstanceOf(PropertiesEditorPresenter.class));
        view1.expects(once()).method("display").with(eq(dataset));

        PropertiesEditorView view1Proxy = (PropertiesEditorView) view1.proxy();
        layout.expects(once()).method("add").with(eq(view1Proxy), new IsInstanceOf(String.class));

        presenter.doShowProperties(view1Proxy, dataset);

        // 2nd attempt - view1 is closed, view2 will be displayed
        view1.stubs().method("isAlive").withNoArguments().will(returnValue(Boolean.FALSE));

        Mock view2 = mock(PropertiesEditorView.class);
        view2.expects(once()).method("observe").with(new IsInstanceOf(PropertiesEditorPresenter.class));
        view2.expects(once()).method("display").with(eq(dataset));

        PropertiesEditorView view2Proxy = (PropertiesEditorView) view2.proxy();
        layout.expects(once()).method("add").with(eq(view2Proxy), new IsInstanceOf(String.class));

        presenter.doShowProperties(view2Proxy, dataset);
    }
}
