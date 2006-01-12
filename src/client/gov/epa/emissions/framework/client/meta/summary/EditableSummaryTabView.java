package gov.epa.emissions.framework.client.meta.summary;

import gov.epa.emissions.framework.client.ChangesNotifier;
import gov.epa.emissions.framework.services.EmfDataset;

public interface EditableSummaryTabView extends ChangesNotifier {
    // update dataset with the view contents
    void updateDataset(EmfDataset dataset);

}
