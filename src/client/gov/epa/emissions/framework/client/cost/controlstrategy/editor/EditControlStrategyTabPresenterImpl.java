package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public class EditControlStrategyTabPresenterImpl implements EditControlStrategyTabPresenter {

    private EditControlStrategyTabView view;

    public EditControlStrategyTabPresenterImpl(ControlStrategy controlStrategy, EditControlStrategyTabView view) {
        this.view = view;
    }

    public void doSave(ControlStrategy controlStrategy) throws EmfException {
        view.save(controlStrategy);
    }

    public void doRefresh(ControlStrategy controlStrategy, ControlStrategyResult[] controlStrategyResults) {
        view.refresh(controlStrategy, controlStrategyResults);
    }
}
