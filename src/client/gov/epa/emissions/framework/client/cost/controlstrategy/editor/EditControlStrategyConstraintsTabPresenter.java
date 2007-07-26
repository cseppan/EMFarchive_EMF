package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyConstraint;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public class EditControlStrategyConstraintsTabPresenter  implements EditControlStrategyTabPresenter {
    private ControlStrategyConstraintsTabView view;
    
    private ControlStrategy strategy;

    public EditControlStrategyConstraintsTabPresenter(ControlStrategyConstraintsTabView view, 
            ControlStrategy strategy, EmfSession session) {
        this.strategy = strategy;
        this.view = view;
    }
    
    public void doDisplay() {
        view.observe(this);
        view.display(this.strategy);
    }

    public ControlStrategyConstraint getConstraint() {
        return strategy.getConstraint();
    }

    public void setConstraint(ControlStrategyConstraint constraint) {
        strategy.setConstraint(constraint);
    }

    public void doRefresh(ControlStrategyResult[] controlStrategyResults) {
        // NOTE Auto-generated method stub
        
    }

    public void doSave() throws EmfException {
        view.save(strategy);
    }
}
