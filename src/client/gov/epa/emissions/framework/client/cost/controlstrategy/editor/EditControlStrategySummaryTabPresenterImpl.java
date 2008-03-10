package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public class EditControlStrategySummaryTabPresenterImpl implements EditControlStrategySummaryTabPresenter {

    private EditControlStrategySummaryTabView view;

    private EditControlStrategyPresenter mainPresenter;

    public EditControlStrategySummaryTabPresenterImpl(EditControlStrategyPresenter mainPresenter, ControlStrategy controlStrategy,
            EditControlStrategySummaryTabView view) {
        this.mainPresenter = mainPresenter;
        this.view = view;
    }

    public void doSave(ControlStrategy controlStrategy) throws EmfException {
        view.save(controlStrategy);
    }

    public void setResults(ControlStrategy controlStrategy) {
        view.setRunMessage(controlStrategy);
    }

    public void doRefresh(ControlStrategyResult[] controlStrategyResults) {
        view.refresh(controlStrategyResults);
    }

    public void doChangeStrategyType(StrategyType strategyType) {
        mainPresenter.doChangeStrategyType(strategyType);
    }

}
