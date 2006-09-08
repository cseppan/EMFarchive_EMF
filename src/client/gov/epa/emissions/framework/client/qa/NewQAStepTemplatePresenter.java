package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;

public class NewQAStepTemplatePresenter {

    private NewQAStepTemplateView view;
    
    private QAStepTemplatesPanelView parentView;

    public NewQAStepTemplatePresenter(QAStepTemplatesPanelView parentView, NewQAStepTemplateView view) {
        this.view = view;
        this.parentView = parentView;
    }
    
    public void display(DatasetType type, QAProgram[] programs) {
        view.observe(this);
        view.display(type, programs);
    }
    
    public void addNew(QAStepTemplate template) {
        parentView.add(template);
    }
}
