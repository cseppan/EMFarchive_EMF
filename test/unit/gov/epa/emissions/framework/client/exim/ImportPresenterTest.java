package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
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
        model.expects(once()).method("importDatasetUsingSingleFile");

        ImportPresenter presenter = new ImportPresenter((EmfSession) session.proxy(), user, (ExImService) model.proxy());

        prefs.stubs().method("mapLocalInputPathToRemote");
        presenter.doImportDatasetUsingSingleFile("dir", "filename", "dataset", type);
    }

    public void testDuringImportRaisesExceptionOnBlankFilename() {
        ImportPresenter presenter = new ImportPresenter((EmfSession) session.proxy(), null, null);

        try {
            presenter.doImportDatasetUsingSingleFile("dir", "", "dataset name", new DatasetType("ORL NonRoad"));
        } catch (EmfException e) {
            assertEquals("Filename should be specified", e.getMessage());
            return;
        }

        fail("should have raised an exception if a blank filename is provided");
    }

    public void testDuringImportRaisesExceptionOnBlankDatasetName() {
        ImportPresenter presenter = new ImportPresenter((EmfSession) session.proxy(), null, null);

        try {
            presenter.doImportDatasetUsingSingleFile("dir", "filename", "", new DatasetType("ORL NonRoad"));
        } catch (EmfException e) {
            assertEquals("Dataset Name should be specified", e.getMessage());
            return;
        }

        fail("should have raised an exception if a blank filename is provided");
    }

    public void testDuringImportRaisesExceptionOnBlankFolder() {
        ImportPresenter presenter = new ImportPresenter((EmfSession) session.proxy(), null, null);

        try {
            presenter.doImportDatasetUsingSingleFile("", "file.txt", "dataset name", new DatasetType("ORL NonRoad"));
        } catch (EmfException e) {
            assertEquals("Folder should be specified", e.getMessage());
            return;
        }

        fail("should have raised an exception if a blank filename is provided");
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
