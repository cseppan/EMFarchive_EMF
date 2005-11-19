package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataService;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class UpdateSectorPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() {
        Sector s = new Sector();

        Mock view = mock(UpdateSectorView.class);

        UpdateSectorPresenter presenter = new UpdateSectorPresenter((UpdateSectorView) view.proxy(), s, null);
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").with(eq(s));

        presenter.doDisplay();
    }

    public void testShouldCloseViewOnClose() {
        Sector s = new Sector();
        Mock view = mock(UpdateSectorView.class);
        view.expects(once()).method("close");

        UpdateSectorPresenter presenter = new UpdateSectorPresenter((UpdateSectorView) view.proxy(), s, null);

        presenter.doClose();
    }

    public void testShouldUpdateSectorAndCloseOnSave() throws EmfException {
        Sector s = new Sector();
        Mock services = mock(DataService.class);
        services.expects(once()).method("updateSector").with(same(s));

        Mock view = mock(UpdateSectorView.class);
        view.expects(once()).method("close");
        UpdateSectorPresenter presenter = new UpdateSectorPresenter((UpdateSectorView) view.proxy(), s,
                (DataService) services.proxy());

        Mock sectorManagerView = mock(SectorsManagerView.class);
        sectorManagerView.expects(once()).method("refresh").withNoArguments();
        
        presenter.doSave((SectorsManagerView)sectorManagerView.proxy());
    }

}
