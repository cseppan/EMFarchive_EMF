package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;

public class NewQAStepTemplatePresenter {

    private NewQAStepTemplateView view;
    
    private EditQAStepTemplatesView parentView;

    public NewQAStepTemplatePresenter(EditQAStepTemplatesView parentView, NewQAStepTemplateView view) {
        this.view = view;
        this.parentView = parentView;
    }
    
    public void display(DatasetType type) {
        view.observe(this);
        view.display(type);
    }
    
    public void addNew(QAStepTemplate template) {
        parentView.add(template);
    }
}
