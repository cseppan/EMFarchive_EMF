package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.Keyword;
import gov.epa.emissions.framework.services.EmfDataset;

public class KeywordsTabPresenter {

    private KeywordsTabView view;

    private EmfDataset dataset;

    public KeywordsTabPresenter(KeywordsTabView view, EmfDataset dataset) {
        this.view = view;
        this.dataset = dataset;
    }

    public void init(Keyword[] keywords) {
        view.display(dataset.getKeyVals(), keywords);
    }

    public void doSave() {
        view.update(dataset);
    }

}
