package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class KeywordsTabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() {
        KeyVal[] values = new KeyVal[] { new KeyVal(), new KeyVal() };
        Mock view = mock(KeywordsTabView.class);
        view.expects(once()).method("display").with(eq(values));

        Mock dataset = mock(EmfDataset.class);
        dataset.stubs().method("getKeyVals").will(returnValue(values));

        KeywordsTabPresenter presenter = new KeywordsTabPresenter(((KeywordsTabView) view.proxy()),
                (EmfDataset) dataset.proxy());

        presenter.display();
    }

}
