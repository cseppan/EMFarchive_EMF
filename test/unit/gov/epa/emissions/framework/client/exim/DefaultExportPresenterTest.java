package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.ExImService;

import java.util.Date;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.jmock.core.constraint.IsInstanceOf;

public class DefaultExportPresenterTest extends MockObjectTestCase {

    private Mock session;

    private String folder;

    private Mock prefs;

    protected void setUp() {
        session = mock(EmfSession.class);

        session.stubs().method("user").withNoArguments().will(returnValue(null));
        session.stubs().method("eximService").withNoArguments().will(returnValue(null));

        folder = "foo/blah";
        session.stubs().method("getMostRecentExportFolder").withNoArguments().will(returnValue(folder));
        setPreferences(session, folder);
    }

    private void setPreferences(Mock session, String folder) {
        prefs = mock(UserPreference.class);
        session.stubs().method("preferences").will(returnValue(prefs.proxy()));
        prefs.stubs().method("mapLocalOutputPathToRemote").will(returnValue(folder));
        prefs.stubs().method("outputFolder").will(returnValue(folder));
    }

    public void testSendsExportRequestToEximServiceOnExport() throws Exception {
        User user = new User();
        user.setUsername("user");
        user.setName("full name");
        Mock dataset = mock(EmfDataset.class);
        dataset.expects(once()).method("setAccessedDateTime").with(new IsInstanceOf(Date.class));

        EmfDataset[] datasets = new EmfDataset[] { (EmfDataset) dataset.proxy() };

        Mock model = mock(ExImService.class);
        model.expects(once()).method("startExportWithOverwrite");

        session.stubs().method("user").withNoArguments().will(returnValue(user));
        session.stubs().method("eximService").withNoArguments().will(returnValue(model.proxy()));
        session.expects(once()).method("setMostRecentExportFolder").with(eq(folder));

        ExportPresenter presenter = new DefaultExportPresenter((EmfSession) session.proxy());

        presenter.doExportWithOverwrite(datasets, folder, "mock export");
    }

    public void testSendsExportRequestToEximServiceOnExportWithoutOverwrite() throws Exception {
        User user = new User();
        user.setUsername("user");
        user.setName("full name");
        String description = "HELLO EMF ACCESSLOGS FOR MOCK EXPORT";
        EmfDataset dataset = new EmfDataset();
        dataset.setName("dataset test");

        EmfDataset[] datasets = new EmfDataset[] { dataset };

        Mock model = mock(ExImService.class);
        model.expects(once()).method("startExport").with(
                new Constraint[] { eq(user), eq(datasets), eq(folder), eq(description) });

        session.stubs().method("user").withNoArguments().will(returnValue(user));
        session.stubs().method("eximService").withNoArguments().will(returnValue(model.proxy()));
        session.expects(once()).method("setMostRecentExportFolder").with(eq(folder));

        ExportPresenter presenter = new DefaultExportPresenter((EmfSession) session.proxy());

        presenter.doExport(datasets, folder, description);
    }

    public void testClosesViewOnDoneExport() {
        Mock view = mock(ExportView.class);
        view.expects(once()).method("close");

        ExportPresenter presenter = new DefaultExportPresenter((EmfSession) session.proxy());

        ExportView viewProxy = (ExportView) view.proxy();
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").withNoArguments();
        view.expects(once()).method("setMostRecentUsedFolder").with(eq(""));

        presenter.display(viewProxy);

        presenter.notifyDone();
    }

    public void testShouldRegisterWithViewOnObserve() {
        session.stubs().method("user").withNoArguments().will(returnValue(null));
        session.stubs().method("eximService").withNoArguments().will(returnValue(null));

        ExportPresenter presenter = new DefaultExportPresenter((EmfSession) session.proxy());

        Mock view = mock(ExportView.class);
        ExportView viewProxy = (ExportView) view.proxy();
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").withNoArguments();
        view.expects(once()).method("setMostRecentUsedFolder").with(eq(""));

        presenter.display(viewProxy);
    }

}
