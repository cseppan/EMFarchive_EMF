package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.EmfDataset;

public interface MetadataView {

    void register(MetadataPresenter presenter);

    void display(EmfDataset dataset);

    void close();
}
