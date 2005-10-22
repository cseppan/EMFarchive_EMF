package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.Sector;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class SectorManagerPresenterTest extends MockObjectTestCase {

	public void testShouldDisplayViewOnDisplay() throws Exception {
		Sector[] sectors = {new Sector(), new Sector()};
		
		Mock view = mock(SectorManagerView.class);
		view.expects(once()).method("display").with(eq(sectors));
		
		Mock service = mock(DataServices.class);
		service.stubs().method("getSectors").withNoArguments().will(returnValue(sectors));
		
		SectorManagerPresenter p = new SectorManagerPresenter((SectorManagerView)view.proxy(), (DataServices)service.proxy());
		
		p.doDisplay();
	}
}
