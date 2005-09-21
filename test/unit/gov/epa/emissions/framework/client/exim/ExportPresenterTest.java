package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;

public class ExportPresenterTest extends MockObjectTestCase {

    private Mock session;

    private String folder;

    protected void setUp() {
        session = mock(EmfSession.class);

        session.stubs().method("getUser").withNoArguments().will(returnValue(null));
        session.stubs().method("getExImServices").withNoArguments().will(returnValue(null));

        folder = "foo/blah";
        session.stubs().method("getMostRecentExportFolder").withNoArguments().will(returnValue(folder));
    }

    public void testSendsExportRequestToEximServiceOnExport() throws EmfException {
        User user = new User();
        user.setUsername("user");
        user.setFullName("full name");
        String purpose = "HELLO EMF ACCESSLOGS FOR MOCK EXPORT";
        EmfDataset dataset = new EmfDataset();
        dataset.setName("dataset test");

        EmfDataset[] datasets = new EmfDataset[] { dataset };

        Mock model = mock(ExImServices.class);
        model.expects(once()).method("startExport").with(
                new Constraint[] { eq(user), eq(datasets), eq(folder), eq(true), eq(purpose) });

        session.stubs().method("getUser").withNoArguments().will(returnValue(user));
        session.stubs().method("getExImServices").withNoArguments().will(returnValue(model.proxy()));
        session.expects(once()).method("setMostRecentExportFolder").with(eq(folder));

        ExportPresenter presenter = new ExportPresenter((EmfSession) session.proxy());

        presenter.notifyExport(datasets, folder, purpose);
    }

    public void testSendsExportRequestToEximServiceOnExportWithoutOverwrite() throws EmfException {
        User user = new User();
        user.setUsername("user");
        user.setFullName("full name");
        String description = "HELLO EMF ACCESSLOGS FOR MOCK EXPORT";
        EmfDataset dataset = new EmfDataset();
        dataset.setName("dataset test");

        EmfDataset[] datasets = new EmfDataset[] { dataset };

        Mock model = mock(ExImServices.class);
        model.expects(once()).method("startExport").with(
                new Constraint[] { eq(user), eq(datasets), eq(folder), eq(false), eq(description) });

        session.stubs().method("getUser").withNoArguments().will(returnValue(user));
        session.stubs().method("getExImServices").withNoArguments().will(returnValue(model.proxy()));
        session.expects(once()).method("setMostRecentExportFolder").with(eq(folder));

        ExportPresenter presenter = new ExportPresenter((EmfSession) session.proxy());

        presenter.notifyExportWithoutOverwrite(datasets, folder, description);
    }

    public void testClosesViewOnDoneExport() {
        Mock view = mock(ExportView.class);
        view.expects(once()).method("close");

        ExportPresenter presenter = new ExportPresenter((EmfSession) session.proxy());

        ExportView viewProxy = (ExportView) view.proxy();
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").withNoArguments();
        view.expects(once()).method("setMostRecentUsedFolder").with(eq(folder));

        presenter.display(viewProxy);

        presenter.notifyDone();
    }

    public void testShouldRegisterWithViewOnObserve() {
        session.stubs().method("getUser").withNoArguments().will(returnValue(null));
        session.stubs().method("getExImServices").withNoArguments().will(returnValue(null));

        ExportPresenter presenter = new ExportPresenter((EmfSession) session.proxy());

        Mock view = mock(ExportView.class);
        ExportView viewProxy = (ExportView) view.proxy();
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").withNoArguments();
        view.expects(once()).method("setMostRecentUsedFolder").with(eq(folder));

        presenter.display(viewProxy);
    }

}
