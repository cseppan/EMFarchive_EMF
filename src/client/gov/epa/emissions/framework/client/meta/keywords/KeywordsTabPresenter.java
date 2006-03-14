package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class KeywordsTabPresenter {

    private KeywordsTabView view;

    private EmfDataset dataset;

    public KeywordsTabPresenter(KeywordsTabView view, EmfDataset dataset) {
        this.view = view;
        this.dataset = dataset;
    }

    public void display() {
        KeyVal[] keyVals = dataset.getKeyVals();
        view.display(keyVals);
    }

}
