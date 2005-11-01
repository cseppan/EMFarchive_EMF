package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.EmfKeyVal;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class KeywordsTabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() {
        Mock view = mock(KeywordsTabView.class);

        Mock dataset = mock(EmfDataset.class);
        EmfKeyVal[] values = new EmfKeyVal[] { new EmfKeyVal(), new EmfKeyVal() };
        dataset.stubs().method("getKeyVals").will(returnValue(values));
        view.expects(once()).method("display").with(same(values));

        KeywordsTabPresenter presenter = new KeywordsTabPresenter((KeywordsTabView) view.proxy(), (EmfDataset) dataset
                .proxy());

        presenter.init();
    }

    public void testUpdateDatasetOnSave() {
        Mock dataset = mock(EmfDataset.class);
        EmfDataset datasetProxy = (EmfDataset) dataset.proxy();

        Mock view = mock(KeywordsTabView.class);
        view.expects(once()).method("update").with(same(datasetProxy));

        KeywordsTabPresenter presenter = new KeywordsTabPresenter((KeywordsTabView) view.proxy(), datasetProxy);

        presenter.doSave();
    }
}
