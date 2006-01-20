package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.services.DataViewService;
import gov.epa.emissions.framework.services.EmfDataset;

public interface DataTabView {

    void display(EmfDataset dataset, DataViewService service);
}
