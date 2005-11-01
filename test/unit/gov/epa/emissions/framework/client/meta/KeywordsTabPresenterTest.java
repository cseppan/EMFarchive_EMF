package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.EmfKeyVal;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class KeywordsTabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() {
        Mock view = mock(KeywordsTabView.class);

        KeywordsTabPresenter presenter = new KeywordsTabPresenter((KeywordsTabView) view.proxy(), null);
        view.expects(once()).method("observe").with(eq(presenter));
        view.expects(once()).method("display").withNoArguments();

        presenter.doDisplay();
    }

    public void testUpdateDatasetOnSave() {
        Mock dataset = mock(EmfDataset.class);
        EmfKeyVal[] values = new EmfKeyVal[] { new EmfKeyVal(), new EmfKeyVal() };
        dataset.expects(once()).method("setKeyVals").with(same(values));
        
        Mock view = mock(KeywordsTabView.class);
        KeywordsTabPresenter presenter = new KeywordsTabPresenter((KeywordsTabView) view.proxy(), (EmfDataset) dataset
                .proxy());

        presenter.doSave(values);
    }
}
