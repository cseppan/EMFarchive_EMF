package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.data.DatasetsBrowserView;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImServices;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class DatasetsBrowserAwareImportPresenterTest extends MockObjectTestCase {

    private Mock eximServices;

    private Mock view;

    private ImportPresenter presenter;

    private Mock dataServices;

    private Mock datasetsBrowser;

    protected void setUp() throws EmfException {
        eximServices = mock(ExImServices.class);
        String folder = "/blah/blagh";
        eximServices.stubs().method("getImportBaseFolder").will(returnValue(folder));

        view = mock(ImportView.class);
        dataServices = mock(DataServices.class);
        datasetsBrowser = mock(DatasetsBrowserView.class);

        presenter = new DatasetsBrowserAwareImportPresenter(null, (ExImServices) eximServices.proxy(), (DataServices) dataServices
                .proxy(), (DatasetsBrowserView) datasetsBrowser.proxy());
        // should register with the view, set default folder, and display the
        // view
        view.expects(once()).method("register").with(eq(presenter));
        view.expects(once()).method("setDefaultBaseFolder").with(eq(folder));
        view.expects(once()).method("display");

        presenter.display((ImportView) view.proxy());
    }

    public void testShouldRefreshDatasetsBrowserAndCloseWindowOnDone() {
        view.expects(once()).method("close");

        EmfDataset[] datasets = new EmfDataset[0];
        dataServices.stubs().method("getDatasets").will(returnValue(datasets));
        datasetsBrowser.expects(once()).method("refresh").with(eq(datasets));

        presenter.doDone();
    }
}
