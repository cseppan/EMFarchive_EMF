package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
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
        Mock updateSectorView = mock(EditableSectorView.class);
        EditableSectorView updateProxy = (EditableSectorView) updateSectorView.proxy();

        Mock layout = mock(ViewLayout.class);
        layout.expects(once()).method("add").with(eq(updateProxy), new IsInstanceOf(Object.class));
        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.FALSE));

        SectorsManagerPresenter p = new SectorsManagerPresenter(null, null, null, (ViewLayout) layout.proxy());

        Mock presenter = mock(EditableSectorPresenter.class);
        presenter.expects(once()).method("doDisplay").withNoArguments();
        EditableSectorPresenter presenterProxy = (EditableSectorPresenter) presenter.proxy();

        Sector sector = new Sector();
        sector.setName("name");

        p.edit(sector, updateProxy, presenterProxy);
    }

    public void testShouldShowDisplaySectorViewOnView() throws Exception {
        Mock view = mock(SectorsManagerView.class);

        Sector sector = new Sector();
        sector.setName("name");

        Mock displaySectorView = mock(ViewableSectorView.class);
        ViewableSectorView viewableProxy = (ViewableSectorView) displaySectorView.proxy();

        Mock layout = mock(ViewLayout.class);
        layout.expects(once()).method("add").with(eq(viewableProxy), new IsInstanceOf(Object.class));
        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.FALSE));

        SectorsManagerPresenter p = new SectorsManagerPresenter(null, (SectorsManagerView) view.proxy(), null,
                (ViewLayout) layout.proxy());

        Mock presenter = mock(ViewableSectorPresenter.class);
        presenter.expects(once()).method("doDisplay").withNoArguments();
        ViewableSectorPresenter presenterProxy = (ViewableSectorPresenter) presenter.proxy();

        
        p.view(sector, viewableProxy, presenterProxy);
    }

    public void testShouldActivateAlreadyDisplayedViewOnRepeatedUpdateOfSameView() throws Exception {
        Sector sector = new Sector();
        sector.setName("name");

        Mock updateSectorView = mock(EditableSectorView.class);
        EditableSectorView updateProxy = (EditableSectorView) updateSectorView.proxy();

        Mock layout = mock(ViewLayout.class);
        layout.expects(once()).method("add").with(eq(updateProxy), new IsInstanceOf(Object.class));
        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.FALSE));

        SectorsManagerPresenter p = new SectorsManagerPresenter(null, null, null, (ViewLayout) layout.proxy());

        Mock presenter = mock(EditableSectorPresenter.class);
        presenter.expects(once()).method("doDisplay").withNoArguments();
        EditableSectorPresenter presenterProxy = (EditableSectorPresenter) presenter.proxy();

        p.edit(sector, updateProxy, presenterProxy);

        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.TRUE));
        p.edit(sector, updateProxy, presenterProxy);
    }

}
