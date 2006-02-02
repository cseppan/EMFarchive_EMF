package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImService;

import java.util.Date;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.jmock.core.constraint.And;
import org.jmock.core.constraint.HasPropertyWithValue;
import org.jmock.core.constraint.IsInstanceOf;

public class ImportPresenterTest extends MockObjectTestCase {

    private Mock model;

    private Mock view;

    private ImportPresenter presenter;
    
    private Mock session;

    protected void FIXME_EmfSession_Setup_Failed_setUp() {
        model = mock(ExImService.class);
        String folder = "/blah/blagh";
        model.stubs().method("getImportBaseFolder").will(returnValue(folder));

        view = mock(ImportView.class);
        session = mock(EmfSession.class);

        presenter = new ImportPresenter((EmfSession)session.proxy(), null, (ExImService) model.proxy());
        // should register with the view, set default folder, and display the
        // view
        view.expects(once()).method("register").with(eq(presenter));
        view.expects(once()).method("setDefaultBaseFolder").with(eq(folder));
        view.expects(once()).method("display");

        presenter.display((ImportView) view.proxy());
    }
    
    public void testRemoveMe() {
        assertTrue(true);
    }

    public void FIXME_EmfSession_Setup_Failed_testSendsImportRequestToEximServiceOnImport() throws Exception {
        DatasetType type = new DatasetType("ORL NonRoad");

        User user = new User();
        user.setUsername("user");
        user.setName("full name");

        String datasetName = "dataset name";
        Constraint datasetNameConstraint = new HasPropertyWithValue("name", eq(datasetName));
        Constraint datasetCreatorConstraint = new HasPropertyWithValue("creator", eq(user.getName()));
        Constraint datasetCreatedDateTimeConstraint = new HasPropertyWithValue("createdDateTime", new IsInstanceOf(
                Date.class));
        Constraint datasetAccessedDateTimeConstraint = new HasPropertyWithValue("accessedDateTime", new IsInstanceOf(
                Date.class));
        Constraint datasetModifiedDateTimeConstraint = new HasPropertyWithValue("modifiedDateTime", new IsInstanceOf(
                Date.class));
        Constraint datasetDateTimeConstraints = new And(new And(datasetCreatedDateTimeConstraint,
                datasetAccessedDateTimeConstraint), datasetModifiedDateTimeConstraint);
        Constraint datasetConstraints = new And(new And(datasetCreatorConstraint, datasetNameConstraint),
                datasetDateTimeConstraints);
        datasetConstraints = new And(new IsInstanceOf(EmfDataset.class), datasetConstraints);

        String dir = "dir";
        String filename = "filename";

        Mock model = mock(ExImService.class);

        Constraint[] constraints = new Constraint[] { eq(user), eq(dir), eq(filename), datasetConstraints };
        model.expects(once()).method("startImport").with(constraints);

        ImportPresenter presenter = new ImportPresenter((EmfSession)session.proxy(), user, (ExImService) model.proxy());

        presenter.doImport(dir, filename, datasetName, type);
    }

    public void FIXME_EmfSession_Setup_Failed_testDuringImportRaisesExceptionOnBlankFilename() {
        ImportPresenter presenter = new ImportPresenter((EmfSession)session.proxy(), null, null);

        try {
            presenter.doImport("dir", "", "dataset name", new DatasetType("ORL NonRoad"));
        } catch (EmfException e) {
            assertEquals("Filename should be specified", e.getMessage());
            return;
        }

        fail("should have raised an exception if a blank filename is provided");
    }

    public void FIXME_EmfSession_Setup_Failed_testDuringImportRaisesExceptionOnBlankDatasetName() {
        ImportPresenter presenter = new ImportPresenter((EmfSession)session.proxy(), null, null);

        try {
            presenter.doImport("dir", "filename", "", new DatasetType("ORL NonRoad"));
        } catch (EmfException e) {
            assertEquals("Dataset Name should be specified", e.getMessage());
            return;
        }

        fail("should have raised an exception if a blank filename is provided");
    }

    public void FIXME_EmfSession_Setup_Failed_testDuringImportRaisesExceptionOnBlankFolder() {
        ImportPresenter presenter = new ImportPresenter((EmfSession)session.proxy(), null, null);

        try {
            presenter.doImport("", "file.txt", "dataset name", new DatasetType("ORL NonRoad"));
        } catch (EmfException e) {
            assertEquals("Folder should be specified", e.getMessage());
            return;
        }

        fail("should have raised an exception if a blank filename is provided");
    }

    public void FIXME_EmfSession_Setup_Failed_testClosesViewOnDoneImport() {
        view.expects(once()).method("close");

        presenter.doDone();
    }

    public void FIXME_EmfSession_Setup_Failed_testShouldClearMessagePanelOnEdit() {
        view.expects(once()).method("clearMessagePanel").withNoArguments();

        presenter.notifyBeginInput();
    }
}
