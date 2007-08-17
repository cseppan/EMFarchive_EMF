package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

//import gov.epa.emissions.framework.services.data.EmfDataset;

public class EditQAEmissionsPresenter {
    
    private EditQAEmissionsView view;
    
    private EditQAStepView view2;
        
    public EditQAEmissionsPresenter(EditQAEmissionsView view, EditQAStepView view2) {
        this.view = view;
        this.view2 = view2;
    }

    public void display(EmfDataset dataset, QAStep qaStep) {
        view.observe(this);
        view.display(dataset, qaStep);
    }
    
    public void updateDatasets(Object [] datasets, Object [] invDatasets) {
        view2.updateDatasets(datasets, invDatasets);
    }
    
    /*public void updateDatasetsToEmissionsWindow(EmfDataset [] datasets1, EmfDataset [] invDatasets1){
       view.updateDatasetsToEmissionsWindow(datasets1, invDatasets1); 
    }*/
    
}
