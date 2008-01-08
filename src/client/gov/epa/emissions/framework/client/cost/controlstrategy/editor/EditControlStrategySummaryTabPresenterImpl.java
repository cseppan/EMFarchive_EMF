package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;

public class EditControlStrategySummaryTabPresenterImpl implements EditControlStrategySummaryTabPresenter {

    private EditControlStrategySummaryTabView view;

    public EditControlStrategySummaryTabPresenterImpl(ControlStrategy controlStrategy,
            EditControlStrategySummaryTabView view) {
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

}
