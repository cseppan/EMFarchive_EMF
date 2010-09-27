package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
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

    public void doRefresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        this.strategy = controlStrategy;
        view.refresh(controlStrategy, controlStrategyResults);
    }

    public void doSave(ControlStrategy controlStrategy) throws EmfException {
        this.strategy = controlStrategy;
        view.save(controlStrategy);
    }

    public void doRun(ControlStrategy controlStrategy) throws EmfException {
        this.strategy = controlStrategy;
        view.run(controlStrategy);
    }

    public void doChangeStrategyType(StrategyType strategyType) {
        if (view != null)
            view.notifyStrategyTypeChange(strategyType);
    }

    public void doSetTargetPollutants(Pollutant[] pollutants) {
        // NOTE Auto-generated method stub
        
    }
}
