package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.services.EmfDataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeywordsTabPresenter {

    private KeywordsTabView view;

    private EmfDataset dataset;

    public KeywordsTabPresenter(KeywordsTabView view, EmfDataset dataset) {
        this.view = view;
        this.dataset = dataset;
    }

    public void display() {
        Keyword[] datasetTypeKeywords = dataset.getDatasetType().getKeywords();
        KeyVal[] keyVals = dataset.getKeyVals();
        view.display(vals(datasetTypeKeywords, keyVals));
    }

    private KeyVal[] vals(Keyword[] datasetTypesKeywords, KeyVal[] keyVals) {
        List result = new ArrayList();

        for (int i = 0; i < datasetTypesKeywords.length; i++) {
            if (!contains(keyVals, datasetTypesKeywords[i])) {
                KeyVal keyVal = new KeyVal();
                keyVal.setKeyword(datasetTypesKeywords[i]);
                keyVal.setValue("");
                result.add(keyVal);
            }
        }
        result.addAll(Arrays.asList(keyVals));

        return (KeyVal[]) result.toArray(new KeyVal[0]);
    }

    private boolean contains(KeyVal[] keyVals, Keyword keyword) {
        for (int i = 0; i < keyVals.length; i++) {
            if (keyword.equals(keyVals[i].getKeyword())) {
                return true;
            }
        }
        return false;
    }

}
