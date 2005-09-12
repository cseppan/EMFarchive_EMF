package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataServices;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class DatasetsBrowserPresenterTest extends MockObjectTestCase {

    private Mock serviceLocator;

    protected void setUp() {
        serviceLocator = mock(ServiceLocator.class);
        serviceLocator.stubs().method("getExImServices").will(returnValue(null));
    }

    public void testShouldCloseViewOnClickOfCloseButton() {
        Mock view = mock(DatasetsBrowserView.class);

        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter(new EmfSession(null, null));

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
        serviceLocator.stubs().method("getDataServices").withNoArguments().will(returnValue(dataservices.proxy()));
        view.expects(once()).method("refresh").with(eq(datasets));

        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter(new EmfSession(null,
                (ServiceLocator) serviceLocator.proxy()));
        view.expects(once()).method("observe").with(eq(presenter));

        presenter.observe((DatasetsBrowserView) view.proxy());

        presenter.notifyRefresh();
    }

    public void testShouldNotifyBrowserViewToDisplayExportViewOnClickOfExportButton() throws EmfException {
        Mock view = mock(DatasetsBrowserView.class);

        EmfDataset dataset1 = new EmfDataset();
        dataset1.setName("name 1");
        dataset1.setCreator("creator 1");

        EmfDataset dataset2 = new EmfDataset();
        dataset2.setName("name 2");
        dataset2.setCreator("creator 2");

        EmfDataset[] datasets = new EmfDataset[] { dataset1, dataset2 };

        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter(new EmfSession(null,
                (ServiceLocator) serviceLocator.proxy()));
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("showExport").with(eq(datasets), new IsInstanceOf(ExportPresenter.class));

        presenter.observe((DatasetsBrowserView) view.proxy());
        presenter.notifyExport(datasets);
    }

}
