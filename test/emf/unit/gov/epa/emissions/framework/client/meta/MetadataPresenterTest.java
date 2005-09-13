package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.EmfDataset;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class MetadataPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnNotifyDisplay() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");

        Mock view = mock(MetadataView.class);
        view.expects(once()).method("display").with(eq(dataset));

        MetadataPresenter presenter = new MetadataPresenter(dataset);
        presenter.observe((MetadataView) view.proxy());

        presenter.notifyDisplay();
    }
}
