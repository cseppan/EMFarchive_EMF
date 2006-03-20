package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;

public class ViewableQAStepTemplatePresenter {

    private ViewableQAStepTemplateView view;
    
    private QAStepTemplate template;
    
    private DatasetType type;
    
    public ViewableQAStepTemplatePresenter(ViewableQAStepTemplateView view, 
            QAStepTemplate template, DatasetType type) {
        this.view = view;
        this.template = template;
        this.type = type;
    }
    
    public void display() {
        view.display(type, template);
    }

}
