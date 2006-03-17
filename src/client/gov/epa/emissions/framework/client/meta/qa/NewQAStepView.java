package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.services.data.QAStep;

public interface NewQAStepView {

    void display(DatasetType type);
    
    QAStep[] qaSteps();
    
    boolean shouldCreate();
}
