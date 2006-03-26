package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public interface NewCustomQAStepView {

    void display(EmfDataset dataset, Version[] versions);
    
    QAStep step();
    
    boolean shouldCreate();
}
