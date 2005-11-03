package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class KeywordsTabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() {
        Mock view = mock(KeywordsTabView.class);

        Mock dataset = mock(EmfDataset.class);
        KeyVal[] values = new KeyVal[] { new KeyVal(), new KeyVal() };
        dataset.stubs().method("getKeyVals").will(returnValue(values));

        Keyword[] keywords = {};
        view.expects(once()).method("display").with(same(values), same(keywords));

        KeywordsTabPresenter presenter = new KeywordsTabPresenter((KeywordsTabView) view.proxy(), (EmfDataset) dataset
                .proxy());

        presenter.init(keywords);
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
