package gov.epa.emissions.framework.client.meta.summary;

import junit.framework.TestCase;

public class SummaryTabPresenterTest extends TestCase {

    public void testShouldNoOpOnDisplay() {
        SummaryTabPresenter presenter = new SummaryTabPresenter();
        presenter.display();
    }
}
