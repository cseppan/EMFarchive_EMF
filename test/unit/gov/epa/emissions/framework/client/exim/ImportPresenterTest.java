package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.commons.ExImServices;
import gov.epa.emissions.framework.commons.User;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class ImportPresenterTest extends MockObjectTestCase {

    public void testSendsImportRequestToEximServiceOnImport() throws EmfException {
        DatasetType type = new DatasetType("ORL NonRoad");

        Mock view = mock(ImportView.class);
        Mock model = mock(ExImServices.class);
        User user = new User();
        user.setUserName("user");
        model.expects(once()).method("startImport").with(eq(user), eq("filepath"), eq(type));

        ImportPresenter presenter = new ImportPresenter(user, (ExImServices) model.proxy(), (ImportView) view.proxy());

        presenter.notifyImport(type, "filepath");
    }
}
