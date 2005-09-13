package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.EmfDataset;

public class SummaryTabPresenter {

    private EmfDataset dataset;

    private SummaryView view;

    public SummaryTabPresenter(EmfDataset dataset) {
        this.dataset = dataset;
    }

    public void observe(SummaryView view) {
        this.view = view;
    }

    public void notifyDisplay() {
        view.display(dataset);
    }

    public void notifyClose() {
        view.close();
    }

}
