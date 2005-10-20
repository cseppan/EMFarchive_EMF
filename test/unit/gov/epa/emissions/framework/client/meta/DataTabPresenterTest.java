package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.InternalSource;
import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class DataTabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayInternalSourcesOnDisplay() {
        EmfDataset dataset = new EmfDataset();
        dataset.setDatasetid(1);
        dataset.addInternalSource(new InternalSource());

        Mock view = mock(DataTabView.class);
        view.expects(once()).method("displayInternalSources").with(eq(dataset.getInternalSources()));

        DataTabPresenter presenter = new DataTabPresenter((DataTabView) view.proxy(), dataset);

        presenter.doDisplay();
    }

    public void testShouldDoNothingOnSave() {
        DataTabPresenter presenter = new DataTabPresenter(null, null);
        presenter.doSave();
    }

}
