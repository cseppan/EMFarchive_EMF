package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class ExportPresenterTest extends MockObjectTestCase {

    public void testSendsExportRequestToEximServiceOnExport() throws EmfException {
        User user = new User();
        user.setUserName("user");
        user.setFullName("full name");

        EmfDataset dataset = new EmfDataset();
        dataset.setCreator(user.getFullName());
        dataset.setName("dataset test");

        String filename = "filename.txt";

        Mock model = mock(ExImServices.class);
        model.expects(once()).method("startExport").with(eq(user), eq(dataset), eq(filename));

        ExportPresenter presenter = new ExportPresenter(user, (ExImServices) model.proxy());

        presenter.notifyExport(dataset, filename);
    }

    public void testClosesViewOnDoneExport() throws EmfException {
        Mock view = mock(ExportView.class);
        view.expects(once()).method("close");

        ExportView viewProxy = (ExportView) view.proxy();
        ExportPresenter presenter = new ExportPresenter(null, null);
        view.expects(once()).method("register").with(eq(presenter));        
        presenter.observe(viewProxy);
        
        presenter.notifyDone();
    }

    public void testShouldRegisterWithViewOnObserve() throws EmfException {
        Mock view = mock(ExportView.class);

        ExportView viewProxy = (ExportView) view.proxy();
        ExportPresenter presenter = new ExportPresenter(null, null);
        view.expects(once()).method("register").with(eq(presenter));

        presenter.observe(viewProxy);
    }

}
