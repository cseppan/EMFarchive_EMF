package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class SummaryTabPresenterTest extends MockObjectTestCase {

    public void testUpdateDatasetOnSave() {
        EmfDataset dataset = new EmfDataset();
        dataset.setName("dataset");
        
        Mock view = mock(SummaryTabView.class);
        view.expects(once()).method("updateDataset").with(eq(dataset));

        SummaryTabPresenter presenter = new SummaryTabPresenter(dataset, (SummaryTabView) view.proxy());

        presenter.save();
    }
}
