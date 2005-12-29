package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class DisplaySectorPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() throws Exception {
        Sector sector = new Sector();
        Mock view = mock(DisplaySectorView.class);

        DisplaySectorPresenter presenter = new DisplaySectorPresenter((DisplaySectorView) view.proxy(), sector);
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(sector));

        presenter.doDisplay();
    }

    public void testShouldCloseViewOnClose() throws Exception {
        Sector sector = new Sector();
        Mock view = mock(DisplaySectorView.class);
        view.expects(once()).method("close");

        DisplaySectorPresenter presenter = new DisplaySectorPresenter((DisplaySectorView) view.proxy(), sector);

        presenter.doClose();
    }

}
