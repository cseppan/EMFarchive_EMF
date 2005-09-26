package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;

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

    protected void setUp() throws EmfException {
        model = mock(ExImServices.class);
        String folder = "/blah/blagh";
        model.stubs().method("getImportBaseFolder").will(returnValue(folder));

        view = mock(ImportView.class);

        presenter = new ImportPresenter(null, (ExImServices) model.proxy());
        // should register with the view, set default folder, and display the
        // view
        view.expects(once()).method("register").with(eq(presenter));
        view.expects(once()).method("setDefaultBaseFolder").with(eq(folder));
        view.expects(once()).method("display");

        presenter.display((ImportView) view.proxy());
    }

    public void testSendsImportRequestToEximServiceOnImport() throws EmfException {
        DatasetType type = new DatasetType("ORL NonRoad");

        User user = new User();
        user.setUsername("user");
        user.setFullName("full name");

        String datasetName = "dataset name";
        Constraint datasetNameConstraint = new HasPropertyWithValue("name", eq(datasetName));
        Constraint datasetCreatorConstraint = new HasPropertyWithValue("creator", eq(user.getFullName()));
        Constraint datasetCreatedDateTimeConstraint = new HasPropertyWithValue("createdDateTime", new IsInstanceOf(Date.class));
        Constraint datasetAccessedDateTimeConstraint = new HasPropertyWithValue("accessedDateTime", new IsInstanceOf(Date.class));
        Constraint datasetModifiedDateTimeConstraint = new HasPropertyWithValue("modifiedDateTime", new IsInstanceOf(Date.class));
        Constraint datasetDateTimeConstraints = new And(new And(datasetCreatedDateTimeConstraint, datasetAccessedDateTimeConstraint), datasetModifiedDateTimeConstraint);
        Constraint datasetConstraints = new And(new And(datasetCreatorConstraint, datasetNameConstraint), datasetDateTimeConstraints);
        
        String dir = "dir";
        String filename = "filename";

        Mock model = mock(ExImServices.class);

        Constraint[] constraints = new Constraint[] { eq(user), eq(dir), eq(filename), datasetConstraints, eq(type) };
        model.expects(once()).method("startImport").with(constraints);

        ImportPresenter presenter = new ImportPresenter(user, (ExImServices) model.proxy());

        presenter.doImport(dir, filename, datasetName, type);
    }

    public void testDuringImportRaisesExceptionOnBlankFilename() {
        ImportPresenter presenter = new ImportPresenter(null, null);

        try {
            presenter.doImport("dir", "", "dataset name", new DatasetType("ORL NonRoad"));
        } catch (EmfException e) {
            assertEquals("Filename should be specified", e.getMessage());
            return;
        }

        fail("should have raised an exception if a blank filename is provided");
    }

    public void testDuringImportRaisesExceptionOnBlankDatasetName() {
        ImportPresenter presenter = new ImportPresenter(null, null);

        try {
            presenter.doImport("dir", "filename", "", new DatasetType("ORL NonRoad"));
        } catch (EmfException e) {
            assertEquals("Dataset Name should be specified", e.getMessage());
            return;
        }

        fail("should have raised an exception if a blank filename is provided");
    }

    public void testDuringImportRaisesExceptionOnBlankFolder() {
        ImportPresenter presenter = new ImportPresenter(null, null);

        try {
            presenter.doImport("", "file.txt", "dataset name", new DatasetType("ORL NonRoad"));
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
