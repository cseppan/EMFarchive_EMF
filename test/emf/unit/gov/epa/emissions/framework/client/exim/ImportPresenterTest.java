package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.EmfDataset;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.ExImServices;
import gov.epa.emissions.framework.services.User;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class ImportPresenterTest extends MockObjectTestCase {

    public void testSendsImportRequestToEximServiceOnImport() throws EmfException {
        DatasetType type = new DatasetType("ORL NonRoad");

        User user = new User();
        user.setUserName("user");
        user.setFullName("full name");

        Dataset dataset = new EmfDataset();
        dataset.setName("dataset name");
        dataset.setCreator(user.getFullName());

        String filename = "filename";

        Mock model = mock(ExImServices.class);
        model.expects(once()).method("startImport").with(eq(user), eq(filename), eq(dataset), eq(type));

        ImportPresenter presenter = new ImportPresenter(user, (ExImServices) model.proxy(), null);

        presenter.notifyImport(filename, dataset.getName(), type);
    }

    public void testClosesViewOnDoneImport() throws EmfException {
        Mock view = mock(ImportView.class);
        view.expects(once()).method("close");

        ImportPresenter presenter = new ImportPresenter(null, null, (ImportView) view.proxy());

        presenter.notifyDone();
    }

    public void testShouldRegisterWithViewOnObserve() throws EmfException {
        Mock view = mock(ImportView.class);

        ImportPresenter presenter = new ImportPresenter(null, null, (ImportView) view.proxy());
        view.expects(once()).method("register").with(eq(presenter));

        presenter.observe();
    }

}
