package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataCommonsService;

import java.util.Date;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class EditableSectorPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewAfterObtainingLockForSectorOnDisplay() throws Exception {
        Sector sector = new Sector();
        Mock view = mock(EditableSectorView.class);

        User user = new User();
        user.setFullName("name");
        sector.setUsername(user.getFullName());
        sector.setLockDate(new Date());

        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("getSectorLock").with(same(user), same(sector)).will(returnValue(sector));

        Mock session = session(user, service.proxy());

        EditableSectorPresenter presenter = new EditableSectorPresenterImpl((EmfSession) session.proxy(),
                (EditableSectorView) view.proxy(), null, sector);
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(sector));

        presenter.doDisplay();
    }

    private Mock session(User user, Object dataCommonsProxy) {
        Mock session = mock(EmfSession.class);
        session.stubs().method("user").withNoArguments().will(returnValue(user));
        session.stubs().method("dataCommonsService").withNoArguments().will(returnValue(dataCommonsProxy));
        
        return session;
    }

    public void testShouldShowNonEditViewAfterFailingToObtainLockForSectorOnDisplay() throws Exception {
        Sector sector = new Sector();// no lock
        User user = new User();
        user.setFullName("name");

        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("getSectorLock").with(same(user), same(sector)).will(returnValue(sector));

        Mock session = session(user, service.proxy());

        Mock view = mock(ViewableSectorView.class);
        view.expects(once()).method("observe").with(new IsInstanceOf(ViewableSectorPresenterImpl.class));
        view.expects(once()).method("display").with(eq(sector));

        EditableSectorPresenter presenter = new EditableSectorPresenterImpl((EmfSession) session.proxy(), null,
                (ViewableSectorView) view.proxy(), sector);

        presenter.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        Sector sector = new Sector();
        Mock view = mock(EditableSectorView.class);
        view.expects(once()).method("close");

        User user = new User();
        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("releaseSectorLock").with(same(user), same(sector)).will(returnValue(sector));

        Mock session = session(user, service.proxy());

        EditableSectorPresenter presenter = new EditableSectorPresenterImpl((EmfSession) session.proxy(),
                (EditableSectorView) view.proxy(), null, sector);

        presenter.doClose();
    }

    public void testShouldUpdateSectorReleaseLockAndCloseOnSave() throws Exception {
        Sector sector = new Sector();

        Mock view = mock(EditableSectorView.class);
        view.expects(once()).method("close");

        User user = new User();
        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("updateSector").with(same(user), same(sector)).will(returnValue(sector));

        Mock session = session(user, service.proxy());

        EditableSectorPresenter presenter = new EditableSectorPresenterImpl((EmfSession) session.proxy(),
                (EditableSectorView) view.proxy(), null, sector);

        Mock sectorManagerView = mock(SectorsManagerView.class);
        sectorManagerView.expects(once()).method("refresh").withNoArguments();

        presenter.doSave((SectorsManagerView) sectorManagerView.proxy());
    }

}
