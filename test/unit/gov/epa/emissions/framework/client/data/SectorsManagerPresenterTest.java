package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.ui.ViewLayout;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class SectorsManagerPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        Sector[] sectors = { new Sector(), new Sector() };

        Mock service = mock(DataService.class);
        service.stubs().method("getSectors").withNoArguments().will(returnValue(sectors));
        DataService servicesProxy = (DataService) service.proxy();

        Mock view = mock(SectorsManagerView.class);
        view.expects(once()).method("display").with(same(servicesProxy));

        SectorsManagerPresenter p = new SectorsManagerPresenter((SectorsManagerView) view.proxy(), servicesProxy, null);
        view.expects(once()).method("observe").with(eq(p));

        p.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        Mock view = mock(SectorsManagerView.class);
        view.expects(once()).method("close").withNoArguments();

        SectorsManagerPresenter p = new SectorsManagerPresenter((SectorsManagerView) view.proxy(), null, null);

        p.doClose();
    }

    public void testShouldDisplayUpdateSectorViewOnUpdate() throws Exception {
        Mock view = mock(SectorsManagerView.class);

        Sector sector = new Sector();
        sector.setName("name");

        Mock updateSectorView = mock(UpdateSectorView.class);
        updateSectorView.expects(once()).method("observe").with(new IsInstanceOf(UpdateSectorPresenter.class));
        updateSectorView.expects(once()).method("display").with(same(sector));
        UpdateSectorView updateProxy = (UpdateSectorView) updateSectorView.proxy();

        Mock layout = mock(ViewLayout.class);
        layout.expects(once()).method("add").with(eq(updateProxy), new IsInstanceOf(Object.class));
        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.FALSE));

        SectorsManagerPresenter p = new SectorsManagerPresenter((SectorsManagerView) view.proxy(), null,
                (ViewLayout) layout.proxy());

        p.doUpdate(sector, updateProxy);
    }

    public void testShouldActivateAlreadyDisplayedViewOnRepeatedUpdateOfSameView() throws Exception {
        Mock view = mock(SectorsManagerView.class);

        Sector sector = new Sector();
        sector.setName("name");

        Mock updateSectorView = mock(UpdateSectorView.class);
        updateSectorView.expects(once()).method("observe").with(new IsInstanceOf(UpdateSectorPresenter.class));
        updateSectorView.expects(once()).method("display").with(same(sector));
        UpdateSectorView updateProxy = (UpdateSectorView) updateSectorView.proxy();

        Mock layout = mock(ViewLayout.class);
        layout.expects(once()).method("add").with(eq(updateProxy), new IsInstanceOf(Object.class));
        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.FALSE));

        SectorsManagerPresenter p = new SectorsManagerPresenter((SectorsManagerView) view.proxy(), null,
                (ViewLayout) layout.proxy());

        p.doUpdate(sector, updateProxy);

        layout.stubs().method("activate").with(new IsInstanceOf(Object.class)).will(returnValue(Boolean.TRUE));
        p.doUpdate(sector, updateProxy);
    }

}
