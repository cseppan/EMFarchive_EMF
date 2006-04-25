package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;
import gov.epa.emissions.framework.ui.RefreshObserver;

public class ControlStrategiesManagerPresenterImpl implements RefreshObserver, ControlStrategiesManagerPresenter {

    private ControlStrategyManagerView view;

    private EmfSession session;

    public ControlStrategiesManagerPresenterImpl(EmfSession session, ControlStrategyManagerView view) {
        this.session = session;
        this.view = view;
    }

    public void display() throws EmfException {
        view.display(service().getControlStrategies());
        view.observe(this);
    }

    private ControlStrategyService service() {
        return session.controlStrategyService();
    }

    public void doRefresh() throws EmfException {
        view.refresh(service().getControlStrategies());
    }

    public void doClose() {
        view.close();
    }

}
