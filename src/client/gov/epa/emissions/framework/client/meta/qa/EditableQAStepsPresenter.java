package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.EmfDataset;

public class EditableQAStepsPresenter {

    private EditableQATabView view;
    
    public EditableQAStepsPresenter(EditableQATabView view) {
        this.view = view;
    }
    
    public void register() {
        view.observe(this);
    }
    
    public void doSave() {
        view.save();
    }
    
    public void doAdd(NewQAStepView stepview, EmfDataset dataset) {
        stepview.display(dataset.getDatasetType());
        if(stepview.shouldCreate()){
            view.add(stepview.qaSteps());
        }
    }

}
