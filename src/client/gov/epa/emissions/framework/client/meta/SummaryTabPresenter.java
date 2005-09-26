package gov.epa.emissions.framework.client.meta;

import java.util.Date;

import gov.epa.emissions.framework.services.EmfDataset;

public class SummaryTabPresenter {

    private SummaryTabView view;

    private EmfDataset dataset;

    public SummaryTabPresenter(EmfDataset dataset, SummaryTabView view) {
        this.dataset = dataset;
        this.view = view;
    }

    public void doSave() {
        dataset.setModifiedDateTime(new Date());
        view.updateDataset(dataset);
    }

}
