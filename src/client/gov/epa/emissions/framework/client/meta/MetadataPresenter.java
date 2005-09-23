package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EmfDataset;

public class MetadataPresenter {

    private EmfDataset dataset;

    private MetadataView view;

    private DataServices dataServices;

    private SummaryTabPresenter summaryTabPresenter;

    public MetadataPresenter(EmfDataset dataset, DataServices dataServices) {
        this.dataset = dataset;
        this.dataServices = dataServices;
    }

    public void display(MetadataView view) {
        this.view = view;
        view.observe(this);

        view.display(dataset);
    }

    public void doClose() {
        view.close();
    }

    public void doSave() {
        try {
            summaryTabPresenter.doSave();
            dataServices.updateDataset(dataset);
        } catch (EmfException e) {
            view.showError("Could not update dataset - " + dataset.getName());
        }
    }

    public void add(SummaryTabView view) {
        summaryTabPresenter = new SummaryTabPresenter(dataset, view);
    }

}
