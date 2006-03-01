package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.services.ExImService;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class ImportPresenterTest extends MockObjectTestCase {

    private Mock model;

    private Mock view;

    private ImportPresenter presenter;

    private Mock session;

    private Mock prefs;

    protected void setUp() {
        model = mock(ExImService.class);

        view = mock(ImportView.class);
        session = mock(EmfSession.class);

        presenter = new ImportPresenter((EmfSession) session.proxy(), null, (ExImService) model.proxy());
        // should register with the view, set default folder, and display the view
        view.expects(once()).method("register").with(eq(presenter));
        view.expects(once()).method("setDefaultBaseFolder");
        view.expects(once()).method("display");

        setPreferences();

        presenter.display((ImportView) view.proxy());
    }

    private void setPreferences() {
        prefs = mock(UserPreference.class);
        prefs.stubs().method("inputFolder").will(returnValue("input"));
        session.stubs().method("preferences").will(returnValue(prefs.proxy()));
    }

    public void testSendsImportRequestToEximServiceOnImport() throws Exception {
        DatasetType type = new DatasetType("ORL NonRoad");

        User user = new User();
        user.setUsername("user");
        user.setName("full name");

        Mock model = mock(ExImService.class);
        model.expects(once()).method("importDataset");

        ImportPresenter presenter = new ImportPresenter((EmfSession) session.proxy(), user, (ExImService) model.proxy());

        prefs.stubs().method("mapLocalInputPathToRemote");
        presenter.doImport("dir", new String[] {"filename"}, type, "");
    }
    
    public void testSendsImportRequestToEximServiceOnImportMultipleDatasets() throws Exception {
        DatasetType type = new DatasetType("ORL NonRoad");

        User user = new User();
        user.setUsername("user");
        user.setName("full name");

        Mock model = mock(ExImService.class);
        model.expects(once()).method("importDatasets");

        ImportPresenter presenter = new ImportPresenter((EmfSession) session.proxy(), user, (ExImService) model.proxy());

        prefs.stubs().method("mapLocalInputPathToRemote");
        presenter.doImport("dir", new String[] {"filename"}, type);
    }

    public void testClosesViewOnDoneImport() {
        view.expects(once()).method("close");

        presenter.doDone();
    }

    public void testShouldClearMessagePanelOnEdit() {
        view.expects(once()).method("clearMessagePanel").withNoArguments();

        presenter.notifyBeginInput();
    }
}
