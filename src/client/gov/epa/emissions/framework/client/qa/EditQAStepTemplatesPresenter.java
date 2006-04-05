package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.services.EmfException;

public interface EditQAStepTemplatesPresenter {
    
    void display(DatasetType type, QAStepTemplate template);
    
    void doEdit() throws EmfException;
}
