package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.services.EmfException;

public class EditQAStepTemplatesPresenterImpl implements EditQAStepTemplatesPresenter {

    private EditQAStepTemplateView view;
    
    private QAStepTemplatesPanelView parentView;

    public EditQAStepTemplatesPresenterImpl(EditQAStepTemplateView view, 
            QAStepTemplatesPanelView parentView) {
        this.view = view;
        this.parentView = parentView;
    }
    
    public void display(DatasetType type,QAProgram[] programs, QAStepTemplate template) {
        view.observe(this);
        view.display(type,programs);
        view.populateFields(template);
    }
    
    public void doEdit() throws EmfException {
        view.loadTemplate();
        parentView.refresh();
    }

}
