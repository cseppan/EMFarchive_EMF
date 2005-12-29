package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.DataCommonsService;
import gov.epa.emissions.framework.ui.ViewLayout;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class SectorsManagerPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        Sector[] sectors = { new Sector(), new Sector() };

        Mock service = mock(DataCommonsService.class);
        service.stubs().method("getSectors").withNoArguments().will(returnValue(sectors));
        DataCommonsService servicesProxy = (DataCommonsService) service.proxy();

        Mock view = mock(SectorsManagerView.class);
        view.expects(once()).method("display").with(same(servicesProxy));

        SectorsManagerPresenter p = new SectorsManagerPresenter(null, (SectorsManagerView) view.proxy(), servicesProxy,
                null);
        view.expects(once()).method("observe").with(eq(p));

        p.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        Mock view = mock(SectorsManagerView.class);
        view.expects(once()).method("close").withNoArguments();

        SectorsManagerPresenter p = new SectorsManagerPresenter(null, (SectorsManagerView) view.proxy(), null, null);

        p.doClose();
    }

    public void testShouldDisplayEditSectorViewOnEdit() throws Exception {
        Mock view = mock(SectorsManagerView.class);

        Sector sector = new Sector();
        sector.setName("name");

        Mock updateSectorView = mock(EditSectorView.class);
        updateSectorView.expects(once()).method("observe").with(new IsInstanceOf(EditSectorPresenter.class));
        updateSectorView.expects(once()).method("display").with(same(sector));
        EditSectorView updateProxy = (EditSectorView) updateSectorView.proxy();

        Mock layout = mock(ViewLayout.class);
        layout.expects(once()).method("add").with(eq(updateProxy), new IsInstanceOf(Object.class));
        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.FALSE));

        User user = new User();
        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("getSectorLock").with(same(user), same(sector)).will(returnValue(sector));

        Mock session = mock(EmfSession.class);
        session.expects(once()).method("user").withNoArguments().will(returnValue(user));

        SectorsManagerPresenter p = new SectorsManagerPresenter((EmfSession) session.proxy(), (SectorsManagerView) view
                .proxy(), (DataCommonsService) service.proxy(), (ViewLayout) layout.proxy());

        p.doEdit(sector, updateProxy);
    }

    public void testShouldShowDisplaySectorViewOnView() throws Exception {
        Mock view = mock(SectorsManagerView.class);

        Sector sector = new Sector();
        sector.setName("name");

        Mock displaySectorView = mock(DisplaySectorView.class);
        displaySectorView.expects(once()).method("observe").with(new IsInstanceOf(DisplaySectorPresenter.class));
        displaySectorView.expects(once()).method("display").with(same(sector));
        DisplaySectorView displayProxy = (DisplaySectorView) displaySectorView.proxy();

        Mock layout = mock(ViewLayout.class);
        layout.expects(once()).method("add").with(eq(displayProxy), new IsInstanceOf(Object.class));
        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.FALSE));

        SectorsManagerPresenter p = new SectorsManagerPresenter(null, (SectorsManagerView) view.proxy(), null,
                (ViewLayout) layout.proxy());

        p.doView(sector, displayProxy);
    }

    public void testShouldActivateAlreadyDisplayedViewOnRepeatedUpdateOfSameView() throws Exception {
        Mock view = mock(SectorsManagerView.class);

        Sector sector = new Sector();
        sector.setName("name");

        Mock updateSectorView = mock(EditSectorView.class);
        updateSectorView.expects(once()).method("observe").with(new IsInstanceOf(EditSectorPresenter.class));
        updateSectorView.expects(once()).method("display").with(same(sector));
        EditSectorView updateProxy = (EditSectorView) updateSectorView.proxy();

        Mock layout = mock(ViewLayout.class);
        layout.expects(once()).method("add").with(eq(updateProxy), new IsInstanceOf(Object.class));
        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.FALSE));

        User user = new User();
        Mock service = mock(DataCommonsService.class);
        service.expects(once()).method("getSectorLock").with(same(user), same(sector)).will(returnValue(sector));

        Mock session = mock(EmfSession.class);
        session.expects(once()).method("user").withNoArguments().will(returnValue(user));

        SectorsManagerPresenter p = new SectorsManagerPresenter((EmfSession) session.proxy(), (SectorsManagerView) view
                .proxy(), (DataCommonsService) service.proxy(), (ViewLayout) layout.proxy());

        p.doEdit(sector, updateProxy);

        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.TRUE));
        p.doEdit(sector, updateProxy);
    }

}
