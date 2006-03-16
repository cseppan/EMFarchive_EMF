package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;

public interface NewQAStepTemplateView {
    void display(DatasetType type);
    
    boolean shouldCreate();
    
    QAStepTemplate template();
    
}
