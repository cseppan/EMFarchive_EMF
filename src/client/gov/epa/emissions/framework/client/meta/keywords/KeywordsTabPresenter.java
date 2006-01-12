package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.framework.services.EmfDataset;

public class KeywordsTabPresenter {

    private KeywordsTabView view;

    private EmfDataset dataset;

    public KeywordsTabPresenter(KeywordsTabView view, EmfDataset dataset) {
        this.view = view;
        this.dataset = dataset;
    }

    public void display() {
        view.display(dataset.getKeyVals());
    }

}
