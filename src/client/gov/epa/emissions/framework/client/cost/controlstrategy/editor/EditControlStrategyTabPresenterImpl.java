package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public class EditControlStrategyTabPresenterImpl implements EditControlStrategyTabPresenter {

    private EditControlStrategyTabView view;

    private ControlStrategy controlStrategy;

    public EditControlStrategyTabPresenterImpl(ControlStrategy controlStrategy, EditControlStrategyTabView view) {
        this.controlStrategy = controlStrategy;
        this.view = view;
    }

    public void doSave() throws EmfException {
        view.save(controlStrategy);
    }

    public void doRefresh(ControlStrategyResult result) {
        view.refresh(result);
    }
}
