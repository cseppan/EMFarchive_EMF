package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.EmfDataset;

public class MetadataPresenter {

    private EmfDataset dataset;

    private MetadataView view;

    public MetadataPresenter(EmfDataset dataset) {
        this.dataset = dataset;
    }

    public void notifyDisplay() {
        view.display(dataset);
    }

    public void observe(MetadataView view) {
        this.view = view;
        view.register(this);
    }

    public void notifyClose() {
        view.close();
    }

}
