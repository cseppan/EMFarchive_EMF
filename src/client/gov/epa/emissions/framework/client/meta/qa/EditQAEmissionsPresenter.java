package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public class EditQAEmissionsPresenter {
    
    private EditQAEmissionsView view;
    
    private EditQAStepView editQAStepView;
        
    public EditQAEmissionsPresenter(EditQAEmissionsView view, EditQAStepView view2) {
        this.view = view;
        this.editQAStepView = view2;
    }

    public void display(EmfDataset dataset, QAStep qaStep) {
        view.observe(this);
        view.display(dataset, qaStep);
    }
    
    public void updateInventories(Object [] inventories, Object [] invTables) {
        editQAStepView.updateInventories(inventories, invTables);
    }
   
}
