package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.DefaultEmfSession;
import gov.epa.emissions.framework.client.exim.ExportPresenter;
import gov.epa.emissions.framework.client.meta.MetadataPresenter;
import gov.epa.emissions.framework.client.meta.MetadataView;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImServices;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class DatasetsBrowserPresenterTest extends MockObjectTestCase {

    private Mock serviceLocator;

    private Mock view;

    private DatasetsBrowserPresenter presenter;

    protected void setUp() throws EmfException {
        Mock eximServices = mock(ExImServices.class);
        eximServices.stubs().method("getExportBaseFolder").will(returnValue("folder/blah"));

        serviceLocator = mock(ServiceLocator.class);
        serviceLocator.stubs().method("getExImServices").will(returnValue(eximServices.proxy()));

        view = mock(DatasetsBrowserView.class);

        presenter = new DatasetsBrowserPresenter(new DefaultEmfSession(null, (ServiceLocator) serviceLocator.proxy()));
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").withNoArguments();

        presenter.display((DatasetsBrowserView) view.proxy());
    }

    public void testShouldCloseViewOnClickOfCloseButton() {
        view.expects(once()).method("close").withNoArguments();

        presenter.doClose();
    }

    public void testShouldRefreshViewOnClickOfRefreshButton() throws EmfException {
        EmfDataset[] datasets = new EmfDataset[0];
        Mock dataservices = mock(DataServices.class);
        dataservices.stubs().method("getDatasets").withNoArguments().will(returnValue(datasets));
        serviceLocator.stubs().method("getDataServices").withNoArguments().will(returnValue(dataservices.proxy()));

        view.expects(once()).method("refresh").with(eq(datasets));

        DatasetsBrowserPresenter presenter = new DatasetsBrowserPresenter(new DefaultEmfSession(null,
                (ServiceLocator) serviceLocator.proxy()));
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").withNoArguments();
        view.expects(once()).method("clearMessage").withNoArguments();
        
        presenter.display((DatasetsBrowserView) view.proxy());

        presenter.doRefresh();
    }

    public void testShouldNotifyViewToDisplayExportViewOnClickOfExportButton() throws EmfException {
        EmfDataset dataset1 = new EmfDataset();
        dataset1.setName("name 1");

        EmfDataset dataset2 = new EmfDataset();
        dataset2.setName("name 2");

        EmfDataset[] datasets = new EmfDataset[] { dataset1, dataset2 };
        view.expects(once()).method("showExport").with(eq(datasets), new IsInstanceOf(ExportPresenter.class));
        view.expects(once()).method("clearMessage").withNoArguments();

        presenter.doExport(datasets);
    }

    public void testShouldDisplayInformationalMessageOnClickOfExportButtonIfNoDatasetsAreSelected() throws EmfException {
        EmfDataset[] datasets = new EmfDataset[0];
        String message = "To Export, you will need to select at least one Dataset";
        view.expects(once()).method("showMessage").with(eq(message));

        presenter.doExport(datasets);
    }

    public void testShouldDisplayMetadataViewOnClickOfMetadataButton() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("name");

        view.expects(once()).method("clearMessage").withNoArguments();

        Mock metadataView = mock(MetadataView.class);
        metadataView.expects(once()).method("observe").with(new IsInstanceOf(MetadataPresenter.class));
        metadataView.expects(once()).method("display").with(eq(dataset));

        presenter.notifyShowMetadata((MetadataView) metadataView.proxy(), dataset);
    }
}
