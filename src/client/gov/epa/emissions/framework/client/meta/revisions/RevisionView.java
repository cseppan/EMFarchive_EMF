package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.services.Revision;

public interface RevisionView {
    void display(Revision revision, EmfDataset dataset);
}
