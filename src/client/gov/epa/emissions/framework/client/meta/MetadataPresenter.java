package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.EmfDataset;

public class MetadataPresenter {

    private EmfDataset dataset;

    private MetadataView view;

    public MetadataPresenter(EmfDataset dataset) {
        this.dataset = dataset;
    }

    public void display(MetadataView view) {
        this.view = view;
        view.observe(this);

        view.display(dataset);
    }

    public void doClose() {
        view.close();
    }

}
