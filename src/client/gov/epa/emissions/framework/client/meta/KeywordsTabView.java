package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.EmfKeyVal;

public interface KeywordsTabView {
    void display(EmfKeyVal[] values);

    void update(EmfDataset dataset);
}
