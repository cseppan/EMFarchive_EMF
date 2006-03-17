package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.EmfDataset;

public class QAStepsPresenter {

    private QATabView view;
    
    public QAStepsPresenter(EmfDataset dataset, QATabView view) {
        this.view = view;
    }
    
    public void register() {
        view.observe(this);
    }
    
    public void doSave() {
        view.save();
    }
    
    public void doAdd(NewQAStepView stepview, EmfDataset dataset2) {
        stepview.display(dataset2.getDatasetType());
        if(stepview.shouldCreate()){
            view.add(stepview.qaSteps());
        }
    }

}
