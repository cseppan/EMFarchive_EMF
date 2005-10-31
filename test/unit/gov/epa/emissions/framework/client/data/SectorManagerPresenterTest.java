package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.services.DataServices;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.constraint.IsInstanceOf;

public class SectorManagerPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        Sector[] sectors = { new Sector(), new Sector() };

        Mock service = mock(DataServices.class);
        service.stubs().method("getSectors").withNoArguments().will(returnValue(sectors));
        DataServices servicesProxy = (DataServices) service.proxy();
        
        Mock view = mock(SectorManagerView.class);
        view.expects(once()).method("display").with(same(servicesProxy));
        
        SectorsManagerPresenter p = new SectorsManagerPresenter((SectorManagerView) view.proxy(), servicesProxy);
        view.expects(once()).method("observe").with(eq(p));

        p.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        Mock view = mock(SectorManagerView.class);
        view.expects(once()).method("close").withNoArguments();

        SectorsManagerPresenter p = new SectorsManagerPresenter((SectorManagerView) view.proxy(), null);

        p.doClose();
    }

    public void testShouldDisplayUpdateSectorViewOnUpdate() throws Exception {
        Mock view = mock(SectorManagerView.class);

        Sector sector = new Sector();

        Mock updateSectorView = mock(UpdateSectorView.class);
        updateSectorView.expects(once()).method("observe").with(new IsInstanceOf(UpdateSectorPresenter.class));
        updateSectorView.expects(once()).method("display").with(same(sector));

        SectorsManagerPresenter p = new SectorsManagerPresenter((SectorManagerView) view.proxy(), null);
        p.doUpdateSector(sector, (UpdateSectorView) updateSectorView.proxy());
    }

}
