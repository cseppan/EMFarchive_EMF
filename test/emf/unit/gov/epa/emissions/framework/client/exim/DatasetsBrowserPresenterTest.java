package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataServices;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class DatasetsBrowserPresenterTest extends MockObjectTestCase {

    private Mock serviceLocator;

    protected void setUp() {
        serviceLocator = mock(ServiceLocator.class);
        serviceLocator.stubs().method("getEximServices").will(returnValue(null));
    }

    public void testShouldCloseViewOnClickOfCloseButton() {
        Mock view = mock(DatasetsBrowserView.class);

        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter(null, null);

        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("close").withNoArguments();

        presenter.observe((DatasetsBrowserView) view.proxy());
        presenter.notifyClose();
    }

    public void testShouldRefreshViewOnClickOfRefreshButton() throws EmfException {
        Mock view = mock(DatasetsBrowserView.class);

        EmfDataset[] datasets = new EmfDataset[0];
        Mock dataservices = mock(DataServices.class);
        dataservices.stubs().method("getDatasets").withNoArguments().will(returnValue(datasets));
        serviceLocator.stubs().method("getDataServices").withNoArguments().will(
                returnValue((DataServices) dataservices.proxy()));
        view.expects(once()).method("refresh").with(eq(datasets));

        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter(null, (ServiceLocator) serviceLocator.proxy());
        view.expects(once()).method("observe").with(eq(presenter));
        
        presenter.observe((DatasetsBrowserView) view.proxy());
        
        presenter.notifyRefresh();
    }

    public void testShouldNotifyBrowserViewToDisplayExportViewOnClickOfExportButton() throws EmfException {
        Mock view = mock(DatasetsBrowserView.class);
        EmfDataset dataset = new EmfDataset();
        dataset.setName("name");
        dataset.setCreator("creator");

        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter(null, (ServiceLocator) serviceLocator.proxy());
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("showExport").with(eq(dataset), new IsInstanceOf(ExportPresenter.class));

        presenter.observe((DatasetsBrowserView) view.proxy());
        presenter.notifyExport(dataset);
    }

}
