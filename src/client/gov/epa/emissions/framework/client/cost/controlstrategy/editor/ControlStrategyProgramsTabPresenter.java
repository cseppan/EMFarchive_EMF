package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public class ControlStrategyProgramsTabPresenter implements EditControlStrategyTabPresenter {
    private ControlStrategyProgramsTab view;
    
    private EmfSession session;
    
    private ControlStrategy controlStrategy;

    public ControlStrategyProgramsTabPresenter(ControlStrategyProgramsTab view, 
            ControlStrategy controlStrategy, 
            EmfSession session) {
        this.controlStrategy = controlStrategy;
        this.session = session;
        this.view = view;
    }
    
    public void doDisplay()  {
        view.observe(this);
        view.display(this.controlStrategy);
    }

    public ControlProgram[] getAllControlPrograms() throws EmfException {
        return session.controlProgramService().getControlPrograms();
    }

    public void doChangeStrategyType(StrategyType strategyType) {
        // NOTE Auto-generated method stub
        
    }

    public void doRefresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        // NOTE Auto-generated method stub
        
    }

    public void doSave(ControlStrategy controlStrategy) {
        view.save(controlStrategy);
    }
}