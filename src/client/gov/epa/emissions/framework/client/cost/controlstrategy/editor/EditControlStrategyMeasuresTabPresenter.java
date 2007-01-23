package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlStrategy;

public class EditControlStrategyMeasuresTabPresenter {
    private ControlStrategyMeasuresTabView view;
    
    private EmfSession session;
    
    private ControlStrategy strategy;
    
    public EditControlStrategyMeasuresTabPresenter(ControlStrategyMeasuresTabView view, 
            ControlStrategy strategy, EmfSession session) {
        this.strategy = strategy;
        this.session = session;
        this.view = view;
    }
    
    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display(this.strategy);
    }
    
    public ControlMeasureClass[] getAllClasses() throws EmfException {
        return session.controlMeasureService().getMeasureClasses();
    }
}
