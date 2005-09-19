package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.EmfDataset;

public interface MetadataView {

    void observe(MetadataPresenter presenter);

    void display(EmfDataset dataset);

    void close();

}
