package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataCommonsService;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class EditSectorPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewAfterObtainingLockForSectorOnDisplay() throws Exception {
        Sector sector = new Sector();
        Mock view = mock(EditSectorView.class);

        User user = new User();
        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("getSectorLock").with(same(user), same(sector)).will(returnValue(sector));

        Mock session = mock(EmfSession.class);
        session.expects(once()).method("user").withNoArguments().will(returnValue(user));

        EditSectorPresenter presenter = new EditSectorPresenter((EmfSession) session.proxy(), (EditSectorView) view
                .proxy(), sector, (DataCommonsService) service.proxy());
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(sector));

        presenter.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        Sector sector = new Sector();
        Mock view = mock(EditSectorView.class);
        view.expects(once()).method("close");

        User user = new User();
        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("releaseSectorLock").with(same(user), same(sector)).will(returnValue(sector));

        Mock session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(user));

        EditSectorPresenter presenter = new EditSectorPresenter((EmfSession) session.proxy(), (EditSectorView) view
                .proxy(), sector, (DataCommonsService) service.proxy());

        presenter.doClose();
    }

    public void testShouldUpdateSectorReleaseLockAndCloseOnSave() throws Exception {
        Sector sector = new Sector();

        Mock view = mock(EditSectorView.class);
        view.expects(once()).method("close");

        User user = new User();
        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("updateSector").with(same(user), same(sector)).will(returnValue(sector));
        service.expects(once()).method("releaseSectorLock").with(same(user), same(sector)).will(returnValue(sector));

        Mock session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(user));

        EditSectorPresenter presenter = new EditSectorPresenter((EmfSession) session.proxy(), (EditSectorView) view
                .proxy(), sector, (DataCommonsService) service.proxy());

        Mock sectorManagerView = mock(SectorsManagerView.class);
        sectorManagerView.expects(once()).method("refresh").withNoArguments();

        presenter.doSave((SectorsManagerView) sectorManagerView.proxy());
    }

}
