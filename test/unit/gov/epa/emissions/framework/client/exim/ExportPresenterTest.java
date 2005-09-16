package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

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
    	boolean overwrite = true;
    	User user = new User();
        user.setUsername("user");
        user.setFullName("full name");

        EmfDataset dataset = new EmfDataset();
        dataset.setName("dataset test");

        EmfDataset[] datasets = new EmfDataset[] { dataset };

        Mock model = mock(ExImServices.class);
        model.expects(once()).method("startExport").with(eq(user), eq(datasets), eq(folder), eq(overwrite));

        session.stubs().method("getUser").withNoArguments().will(returnValue(user));
        session.stubs().method("getExImServices").withNoArguments().will(returnValue(model.proxy()));
        session.expects(once()).method("setMostRecentExportFolder").with(eq(folder));

        ExportPresenter presenter = new ExportPresenter((EmfSession) session.proxy());

        presenter.notifyExport(datasets, folder, overwrite);
    }

    public void testClosesViewOnDoneExport() {
        Mock view = mock(ExportView.class);
        view.expects(once()).method("close");

        ExportPresenter presenter = new ExportPresenter((EmfSession) session.proxy());

        ExportView viewProxy = (ExportView) view.proxy();
        view.expects(once()).method("register").with(eq(presenter));
        view.expects(once()).method("setMostRecentUsedFolder").with(eq(folder));

        presenter.observe(viewProxy);

        presenter.notifyDone();
    }

    public void testShouldRegisterWithViewOnObserve() {
        Mock view = mock(ExportView.class);

        session.stubs().method("getUser").withNoArguments().will(returnValue(null));
        session.stubs().method("getExImServices").withNoArguments().will(returnValue(null));

        ExportPresenter presenter = new ExportPresenter((EmfSession) session.proxy());

        ExportView viewProxy = (ExportView) view.proxy();
        view.expects(once()).method("register").with(eq(presenter));
        view.expects(once()).method("setMostRecentUsedFolder").with(eq(folder));

        presenter.observe(viewProxy);
    }

}
