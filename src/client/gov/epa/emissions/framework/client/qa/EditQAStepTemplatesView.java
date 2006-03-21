package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.QAStepTemplate;

public interface EditQAStepTemplatesView {
    void observe(EditQAStepTemplatesPresenter presenter);
    
    void add(QAStepTemplate template);
    
    void refresh();
}
