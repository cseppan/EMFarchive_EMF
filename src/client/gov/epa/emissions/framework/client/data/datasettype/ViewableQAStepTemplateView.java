package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;

public interface ViewableQAStepTemplateView {
    
    void display(DatasetType type, QAStepTemplate template);
}
