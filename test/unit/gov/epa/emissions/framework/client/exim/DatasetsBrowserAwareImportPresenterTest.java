package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.data.DatasetsBrowserView;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImService;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class DatasetsBrowserAwareImportPresenterTest extends MockObjectTestCase {

    private Mock eximServices;

    private Mock view;

    private ImportPresenter presenter;

    private Mock dataServices;

    private Mock datasetsBrowser;

    private Mock session;

    private Mock prefs;

    protected void setUp() {
        eximServices = mock(ExImService.class);

        view = mock(ImportView.class);
        dataServices = mock(DataService.class);
        datasetsBrowser = mock(DatasetsBrowserView.class);

        session = mock(EmfSession.class);
        setPreferences(session);

        presenter = new DatasetsBrowserAwareImportPresenter((EmfSession) session.proxy(), null,
                (ExImService) eximServices.proxy(), (DataService) dataServices.proxy(),
                (DatasetsBrowserView) datasetsBrowser.proxy());
        
        // should register with the view, set default folder, and display the view
        view.expects(once()).method("register").with(eq(presenter));
        view.expects(once()).method("setDefaultBaseFolder").with(eq(""));
        view.expects(once()).method("display");

        presenter.display((ImportView) view.proxy());
    }

    private void setPreferences(Mock session) {
        prefs = mock(UserPreference.class);
        prefs.stubs().method("inputFolder").will(returnValue("input"));
        session.stubs().method("preferences").will(returnValue(prefs.proxy()));
    }

    public void testShouldRefreshDatasetsBrowserAndCloseWindowOnDone() {
        view.expects(once()).method("close");

        EmfDataset[] datasets = new EmfDataset[0];
        dataServices.stubs().method("getDatasets").will(returnValue(datasets));
        datasetsBrowser.expects(once()).method("refresh").with(eq(datasets));

        presenter.doDone();
    }
}
