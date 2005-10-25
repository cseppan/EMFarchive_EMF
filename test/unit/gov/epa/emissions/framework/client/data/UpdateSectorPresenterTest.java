package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataServices;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class UpdateSectorPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() {
        Sector s = new Sector();

        Mock view = mock(UpdateSectorView.class);

        UpdateSectorPresenter presenter = new UpdateSectorPresenter((UpdateSectorView) view.proxy(), s, null);
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(s));

        presenter.doDisplay();
    }

    public void testShouldCloseViewOnClose() {
        Sector s = new Sector();

        Mock view = mock(UpdateSectorView.class);

        UpdateSectorPresenter presenter = new UpdateSectorPresenter((UpdateSectorView) view.proxy(), s, null);
        view.expects(once()).method("close");

        presenter.doClose();
    }

    public void testShouldUpdateSectorOnSave() throws EmfException {
        Sector s = new Sector();
        Mock services = mock(DataServices.class);
        services.expects(once()).method("updateSector").with(same(s));

        UpdateSectorPresenter presenter = new UpdateSectorPresenter(null, s, (DataServices) services.proxy());

        presenter.doSave();
    }

}
