package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.data.Keywords;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabPresenter;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabView;
import gov.epa.emissions.framework.services.EmfDataset;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;

public class EditableKeywordsTabPresenterTest extends MockObjectTestCase {

    public void testShouldDisplayViewOnDisplay() {
        Mock view = mock(EditableKeywordsTabView.class);

        Mock dataset = mock(EmfDataset.class);
        KeyVal[] values = new KeyVal[] { new KeyVal(), new KeyVal() };
        dataset.stubs().method("getKeyVals").will(returnValue(values));

        Keywords keywords = new Keywords(new Keyword[0]);
        view.expects(once()).method("display").with(eq(values), same(keywords));

        Mock type = mock(DatasetType.class);
        type.stubs().method("getKeywords").withNoArguments().will(returnValue(new Keyword[0]));
        dataset.stubs().method("getDatasetType").withNoArguments().will(returnValue(type.proxy()));

        EditableKeywordsTabPresenter presenter = new EditableKeywordsTabPresenter((EditableKeywordsTabView) view.proxy(), (EmfDataset) dataset
                .proxy());

        presenter.init(keywords);
    }

    public void testShouldDisplayKeyValuesForAllKeywordsOfAssociatedDatasetType() {
        final Keyword keyword1 = new Keyword("key1");
        final Keyword keyword2 = new Keyword("key2");
        Keyword[] keywordsList = { keyword1, keyword2 };

        Mock dataset = mock(EmfDataset.class);
        KeyVal keyVal = new KeyVal();
        keyVal.setKeyword(keyword1);
        final KeyVal[] values = new KeyVal[] { keyVal };
        dataset.stubs().method("getKeyVals").will(returnValue(values));

        Mock type = mock(DatasetType.class);
        type.stubs().method("getKeywords").withNoArguments().will(returnValue(keywordsList));
        dataset.stubs().method("getDatasetType").withNoArguments().will(returnValue(type.proxy()));

        Mock view = mock(EditableKeywordsTabView.class);
        Keywords keywords = new Keywords(keywordsList);
        view.expects(once()).method("display").with(keyValsConstraint(keyword1, keyword2, values), same(keywords));

        EditableKeywordsTabPresenter presenter = new EditableKeywordsTabPresenter((EditableKeywordsTabView) view.proxy(), (EmfDataset) dataset
                .proxy());

        presenter.init(keywords);
    }

    private Constraint keyValsConstraint(final Keyword keyword1, final Keyword keyword2, final KeyVal[] values) {
        return new Constraint() {
            public boolean eval(Object arg) {
                assertTrue(arg instanceof KeyVal[]);

                KeyVal[] actual = (KeyVal[]) arg;
                assertEquals(2, actual.length);
                assertEquals(values[0], actual[0]);
                assertEquals(keyword1, actual[0].getKeyword());
                KeyVal newKeyVal = actual[1];
                assertEquals("", newKeyVal.getValue());
                assertEquals(keyword2, newKeyVal.getKeyword());

                return true;
            }

            public StringBuffer describeTo(StringBuffer arg0) {
                return null;
            }
        };
    }

    public void testUpdateDatasetOnSave() throws EmfException {
        KeyVal[] keyvals = {};
        Mock dataset = mock(EmfDataset.class);
        dataset.expects(once()).method("setKeyVals").with(same(keyvals));
        Mock view = mock(EditableKeywordsTabView.class);
        view.expects(once()).method("updates").withNoArguments().will(returnValue(keyvals));

        EditableKeywordsTabPresenter presenter = new EditableKeywordsTabPresenter((EditableKeywordsTabView) view.proxy(), ((EmfDataset) dataset
                .proxy()));

        presenter.doSave();
    }

    public void testShouldFailWithErrorIfDuplicateKeywordsInKeyValsOnSave() {
        KeyVal keyval = new KeyVal();
        keyval.setKeyword(new Keyword("name"));
        KeyVal[] keyvals = { keyval, keyval };

        Mock dataset = mock(EmfDataset.class);
        Mock view = mock(EditableKeywordsTabView.class);
        view.expects(once()).method("updates").withNoArguments().will(returnValue(keyvals));

        EditableKeywordsTabPresenter presenter = new EditableKeywordsTabPresenter((EditableKeywordsTabView) view.proxy(), ((EmfDataset) dataset
                .proxy()));

        try {
            presenter.doSave();
        } catch (EmfException e) {
            assertEquals("duplicate keyword 'name'", e.getMessage());
            return;
        }

        fail("should have raised an error on duplicate keyword entries");
    }
}
