package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public class EditControlStrategyMeasuresTabPresenter  implements EditControlStrategyTabPresenter {
    private ControlStrategyMeasuresTabView view;
    
    private EmfSession session;
    
    private ControlStrategy strategy;
    
    public EditControlStrategyMeasuresTabPresenter(ControlStrategyMeasuresTabView view, 
            ControlStrategy strategy, EmfSession session) {
        this.strategy = strategy;
        this.session = session;
        this.view = view;
    }
    
    public void doDisplay() throws EmfException  {
        view.observe(this);
        view.display(this.strategy);
    }

    public ControlMeasureClass[] getAllClasses() throws EmfException {
        return session.controlMeasureService().getMeasureClasses();
    }

    public ControlMeasureClass[] getControlMeasureClasses() {
        return strategy.getControlMeasureClasses();
    }

    public void doRefresh(ControlStrategyResult result) {
        // NOTE Auto-generated method stub
        
    }

    public void doSave() throws EmfException {
        view.save(strategy);
    }
}
