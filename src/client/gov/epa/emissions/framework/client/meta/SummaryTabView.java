package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.EmfDataset;

public interface SummaryTabView {

    // update dataset with the view contents
    void updateDataset(EmfDataset dataset);
}
