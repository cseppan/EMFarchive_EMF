package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;

public interface NewQAStepTemplateView {
    
    void display(DatasetType type, QAProgram[] programs);

    QAStepTemplate template();

    void observe(NewQAStepTemplatePresenter presenter);

}
