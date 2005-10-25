package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.services.DataServices;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class SectorManagerPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        Sector[] sectors = { new Sector(), new Sector() };

        Mock view = mock(SectorManagerView.class);
        view.expects(once()).method("display").with(eq(sectors));

        Mock service = mock(DataServices.class);
        service.stubs().method("getSectors").withNoArguments().will(returnValue(sectors));

        SectorManagerPresenter p = new SectorManagerPresenter((SectorManagerView) view.proxy(), (DataServices) service
                .proxy());
        view.expects(once()).method("observe").with(eq(p));

        p.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        Mock view = mock(SectorManagerView.class);
        view.expects(once()).method("close").withNoArguments();

        SectorManagerPresenter p = new SectorManagerPresenter((SectorManagerView) view.proxy(), null);

        p.doClose();
    }
}
