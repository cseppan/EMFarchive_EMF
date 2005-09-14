package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class MetadataPresenterTest extends MockObjectTestCase {

    private Mock view;

    private MetadataPresenter presenter;

    private EmfDataset dataset;

    protected void setUp() {
        dataset = new EmfDataset();
        dataset.setName("test");

        view = mock(MetadataView.class);

        presenter = new MetadataPresenter(dataset);
        view.expects(once()).method("register").with(eq(presenter));

        presenter.observe((MetadataView) view.proxy());
    }

    public void testShouldDisplayViewOnNotifyDisplay() {
        view.expects(once()).method("display").with(eq(dataset));

        presenter.notifyDisplay();
    }

    public void testShouldCloseViewOnNotifyClose() {
        view.expects(once()).method("close");

        presenter.notifyClose();
    }
}
