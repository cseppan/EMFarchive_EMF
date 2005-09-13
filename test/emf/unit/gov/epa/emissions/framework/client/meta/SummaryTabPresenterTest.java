package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.EmfDataset;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class SummaryTabPresenterTest extends MockObjectTestCase {

    private Mock view;

    private SummaryTabPresenter presenter;

    private EmfDataset dataset;

    protected void setUp() {
        dataset = new EmfDataset();
        dataset.setName("dataset");

        view = mock(SummaryView.class);

        presenter = new SummaryTabPresenter(dataset);
        presenter.observe((SummaryView) view.proxy());
    }

    public void testShouldDisplayViewWithSummaryInformationOnNotifyDisplay() {
        view.expects(once()).method("display").with(eq(dataset));

        presenter.notifyDisplay();
    }

    public void testShouldCloseViewOnNotifyClose() {
        view.expects(once()).method("close").withNoArguments();

        presenter.notifyClose();
    }
}
