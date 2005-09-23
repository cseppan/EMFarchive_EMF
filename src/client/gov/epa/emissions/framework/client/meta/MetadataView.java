package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.client.EmfView;
import gov.epa.emissions.framework.services.EmfDataset;

public interface MetadataView extends EmfView {

    void observe(MetadataPresenter presenter);

    void display(EmfDataset dataset);

    void showError(String message);

}
