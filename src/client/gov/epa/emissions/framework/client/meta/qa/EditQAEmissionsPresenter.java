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
    
    public void updateInventories(Object [] inventories, Object [] invTables, String summaryType) {
        editQAStepView.updateInventories(inventories, invTables, summaryType);
    }
    
    public void updateInventories(Object [] invBase, Object [] invControl, Object [] invTables, String summaryType) {
        editQAStepView.updateInventories(invBase, invControl, invTables, summaryType);
    }
   
}
