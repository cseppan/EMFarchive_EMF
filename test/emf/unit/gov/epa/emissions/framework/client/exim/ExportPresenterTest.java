package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.EmfSessionStub;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class ExportPresenterTest extends MockObjectTestCase {

    public void testSendsExportRequestToEximServiceOnExport() throws EmfException {
        User user = new User();
        user.setUserName("user");
        user.setFullName("full name");

        EmfDataset dataset = new EmfDataset();
        dataset.setCreator(user.getFullName());
        dataset.setName("dataset test");

        EmfDataset[] datasets = new EmfDataset[] { dataset };
        String directory = "/directory";

        Mock model = mock(ExImServices.class);
        model.expects(once()).method("startExport").with(eq(user), eq(datasets), eq(directory));

        Mock session = mock(EmfSessionStub.class);
        session.stubs().method("getUser").withNoArguments().will(returnValue(user));
        session.stubs().method("getExImServices").withNoArguments().will(returnValue(model.proxy()));

        ExportPresenter presenter = new ExportPresenter((EmfSession) session.proxy());

        presenter.notifyExport(datasets, directory);
    }

    public void testClosesViewOnDoneExport() throws EmfException {
        Mock view = mock(ExportView.class);
        view.expects(once()).method("close");

        Mock session = mock(EmfSessionStub.class);
        session.stubs().method("getUser").withNoArguments().will(returnValue(null));
        session.stubs().method("getExImServices").withNoArguments().will(returnValue(null));

        ExportPresenter presenter = new ExportPresenter((EmfSession) session.proxy());

        ExportView viewProxy = (ExportView) view.proxy();
        view.expects(once()).method("register").with(eq(presenter));
        presenter.observe(viewProxy);

        presenter.notifyDone();
    }

    public void testShouldRegisterWithViewOnObserve() throws EmfException {
        Mock view = mock(ExportView.class);

        Mock session = mock(EmfSessionStub.class);
        session.stubs().method("getUser").withNoArguments().will(returnValue(null));
        session.stubs().method("getExImServices").withNoArguments().will(returnValue(null));

        ExportPresenter presenter = new ExportPresenter((EmfSession) session.proxy());
        
        ExportView viewProxy = (ExportView) view.proxy();
        view.expects(once()).method("register").with(eq(presenter));

        presenter.observe(viewProxy);
    }

}
