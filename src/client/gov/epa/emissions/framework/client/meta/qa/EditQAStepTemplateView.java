package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;

public interface EditQAStepTemplateView {
    void display(DatasetType type);
    
    void observe(EditQAStepTemplatesPresenter presenter);

    void loadTemplate();
    
    void display(QAStepTemplate template);
}
