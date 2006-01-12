package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class PropertiesViewPresenterTest extends MockObjectTestCase {

    public void testShouldObserveAndDisplayViewOnDisplay() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");
        dataset.setDatasetType(new DatasetType());

        Mock view = mock(PropertiesView.class);
        view.expects(once()).method("display").with(eq(dataset));

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset);
        view.expects(once()).method("observe").with(eq(presenter));

        presenter.doDisplay((PropertiesView) view.proxy());
    }

    public void testShouldCloseViewOnClose() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");
        dataset.setDatasetType(new DatasetType());

        Mock view = mock(PropertiesView.class);
        view.expects(once()).method("display").with(eq(dataset));

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset);
        view.expects(once()).method("observe").with(eq(presenter));
        presenter.doDisplay((PropertiesView) view.proxy());

        view.expects(once()).method("close");

        presenter.doClose();
    }

}
