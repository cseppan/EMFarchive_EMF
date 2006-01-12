package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.data.Keywords;
import gov.epa.emissions.framework.services.EmfDataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class EditableKeywordsTabPresenter {

    private EditableKeywordsTabView view;

    private EmfDataset dataset;

    public EditableKeywordsTabPresenter(EditableKeywordsTabView view, EmfDataset dataset) {
        this.view = view;
        this.dataset = dataset;
    }

    public void display(Keywords masterKeywords) {
        view.display(vals(dataset.getDatasetType().getKeywords()), masterKeywords);
    }

    private KeyVal[] vals(Keyword[] datasetTypesKeywords) {
        List result = new ArrayList();

        KeyVal[] keyVals = dataset.getKeyVals();
        result.addAll(Arrays.asList(keyVals));

        for (int i = 0; i < datasetTypesKeywords.length; i++) {
            if (!contains(result, datasetTypesKeywords[i])) {
                KeyVal keyVal = new KeyVal();
                keyVal.setKeyword(datasetTypesKeywords[i]);
                keyVal.setValue("");
                result.add(keyVal);
            }
        }

        return (KeyVal[]) result.toArray(new KeyVal[0]);
    }

    private boolean contains(List keyVals, Keyword keyword) {
        for (Iterator iter = keyVals.iterator(); iter.hasNext();) {
            KeyVal element = (KeyVal) iter.next();
            if (element.getKeyword().equals(keyword))
                return true;
        }

        return false;
    }

    public void doSave() throws EmfException {
        KeyVal[] updates = view.updates();
        verifyDuplicates(updates);

        dataset.setKeyVals(updates);
    }

    private void verifyDuplicates(KeyVal[] updates) throws EmfException {
        Set set = new TreeSet();
        for (int i = 0; i < updates.length; i++) {
            String name = updates[i].getKeyword().getName();
            if (!set.add(name))
                throw new EmfException("duplicate keyword '" + name + "'");
        }
    }

}
