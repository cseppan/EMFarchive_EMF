package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;

public class EditControlStrategySummaryTabPresenterImpl implements EditControlStrategySummaryTabPresenter {

    private EditControlStrategySummaryTabView view;

    private ControlStrategy controlStrategy;

    public EditControlStrategySummaryTabPresenterImpl(ControlStrategy controlStrategy, EditControlStrategySummaryTabView view) {
        this.controlStrategy = controlStrategy;
        this.view = view;
    }

    public void doSave() throws EmfException {
        view.save(controlStrategy);
    }

}
