package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.KeyVal;
import gov.epa.emissions.framework.services.EmfDataset;

public interface KeywordsTabView {
    void display(KeyVal[] values);

    void update(EmfDataset dataset);
}
