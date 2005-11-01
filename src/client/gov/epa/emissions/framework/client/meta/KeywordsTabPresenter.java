package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.EmfKeyVal;

public class KeywordsTabPresenter {

    private KeywordsTabView view;

    private EmfDataset dataset;

    public KeywordsTabPresenter(KeywordsTabView view, EmfDataset dataset) {
        this.view = view;
        this.dataset = dataset;
    }

    public void doDisplay() {
        view.display(dataset.getKeyVals());
    }

    public void doSave(EmfKeyVal[] values) {
        dataset.setKeyVals(values);
    }

}
