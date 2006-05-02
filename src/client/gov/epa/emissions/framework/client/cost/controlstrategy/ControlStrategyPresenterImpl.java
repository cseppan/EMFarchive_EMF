package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;

import java.util.Date;

public class ControlStrategyPresenterImpl implements ControlStrategyPresenter {

    private EmfSession session;

    private ControlStrategyView view;

    private ControlStrategiesManagerPresenter managerPresenter;

    public ControlStrategyPresenterImpl(EmfSession session, ControlStrategyView view,
            ControlStrategiesManagerPresenter controlStrategiesManagerPresenter) {
        this.session = session;
        this.view = view;
        this.managerPresenter = controlStrategiesManagerPresenter;
    }

    public void doDisplay() {
        view.observe(this);
        view.display();
    }

    public void doClose() {
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public void doSave(ControlStrategy newControlStrategy) throws EmfException {
        if (isDuplicate(newControlStrategy))
            throw new EmfException("Duplicate name - '" + newControlStrategy.getName() + "'.");

        newControlStrategy.setCreator(session.user());
        newControlStrategy.setLastModifiedDate(new Date());

        service().addControlStrategy(newControlStrategy);
        closeView();
        managerPresenter.doRefresh();
    }

    private boolean isDuplicate(ControlStrategy newControlStrategy) throws EmfException {
        ControlStrategy[] controlStrategies = service().getControlStrategies();
        for (int i = 0; i < controlStrategies.length; i++) {
            if (controlStrategies[i].getName().equals(newControlStrategy.getName()))
                return true;
        }
        return false;
    }

    private ControlStrategyService service() {
        return session.controlStrategyService();
    }

}
