package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataCommonsService;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class UpdateSectorPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewAfterObtainingLockForSectorOnDisplay() throws Exception {
        Sector sector = new Sector();
        Mock view = mock(UpdateSectorView.class);

        User user = new User();
        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("getSectorLock").with(same(user), same(sector)).will(returnValue(sector));

        Mock session = mock(EmfSession.class);
        session.expects(once()).method("user").withNoArguments().will(returnValue(user));

        UpdateSectorPresenter presenter = new UpdateSectorPresenter((EmfSession) session.proxy(),
                (UpdateSectorView) view.proxy(), sector, (DataCommonsService) service.proxy());
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(sector));

        presenter.doDisplay();
    }

    public void testShouldCloseViewOnClose() {
        Sector s = new Sector();
        Mock view = mock(UpdateSectorView.class);
        view.expects(once()).method("close");

        UpdateSectorPresenter presenter = new UpdateSectorPresenter(null, (UpdateSectorView) view.proxy(), s, null);

        presenter.doClose();
    }

    public void testShouldUpdateSectorAndCloseOnSave() throws EmfException {
        Sector sector = new Sector();
        Mock services = mock(DataCommonsService.class);
        services.expects(once()).method("updateSector").with(same(sector));

        Mock view = mock(UpdateSectorView.class);
        view.expects(once()).method("close");

        UpdateSectorPresenter presenter = new UpdateSectorPresenter(null, (UpdateSectorView) view.proxy(), sector,
                (DataCommonsService) services.proxy());

        Mock sectorManagerView = mock(SectorsManagerView.class);
        sectorManagerView.expects(once()).method("refresh").withNoArguments();

        presenter.doSave((SectorsManagerView) sectorManagerView.proxy());
    }

}
