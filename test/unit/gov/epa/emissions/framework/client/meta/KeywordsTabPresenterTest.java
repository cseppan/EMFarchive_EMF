package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;
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

    public void testUpdateDatasetOnSave() throws EmfException {
        KeyVal[] keyvals = {};
        Mock dataset = mock(EmfDataset.class);
        dataset.expects(once()).method("setKeyVals").with(same(keyvals));
        Mock view = mock(KeywordsTabView.class);
        view.expects(once()).method("updates").withNoArguments().will(returnValue(keyvals));

        KeywordsTabPresenter presenter = new KeywordsTabPresenter((KeywordsTabView) view.proxy(), ((EmfDataset) dataset
                .proxy()));

        presenter.doSave();
    }

    public void testShouldFailWithErrorIfDuplicateKeywordsInKeyValsOnSave() {
        KeyVal keyval = new KeyVal();
        keyval.setKeyword(new Keyword("name"));
        KeyVal[] keyvals = { keyval, keyval };

        Mock dataset = mock(EmfDataset.class);
        Mock view = mock(KeywordsTabView.class);
        view.expects(once()).method("updates").withNoArguments().will(returnValue(keyvals));

        KeywordsTabPresenter presenter = new KeywordsTabPresenter((KeywordsTabView) view.proxy(), ((EmfDataset) dataset
                .proxy()));

        try {
            presenter.doSave();
        } catch (EmfException e) {
            assertEquals("Duplicate keyword: 'name' not allowed", e.getMessage());
            return;
        }

        fail("should have raised an error on duplicate keyword entries");
    }
}
