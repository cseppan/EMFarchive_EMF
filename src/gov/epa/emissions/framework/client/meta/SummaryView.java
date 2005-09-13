package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.EmfDataset;

public interface SummaryView {

    void display(EmfDataset dataset);

    void close();
}
