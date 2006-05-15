package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategySummaryTabPresenter;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategySummaryTabPresenterImpl;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategySummaryTabView;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyTabPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class EditControlStrategyPresenterImpl implements EditControlStrategyPresenter {

    private EmfSession session;

    private EditControlStrategyView view;

    private ControlStrategiesManagerPresenter managerPresenter;

    private ControlStrategy controlStrategy;

    private List presenters;

    public EditControlStrategyPresenterImpl(ControlStrategy controlStrategy, EmfSession session,
            EditControlStrategyView view, ControlStrategiesManagerPresenter controlStrategiesManagerPresenter) {
        this.controlStrategy = controlStrategy;
        this.session = session;
        this.view = view;
        this.managerPresenter = controlStrategiesManagerPresenter;
        this.presenters = new ArrayList();
    }

    public void doDisplay() throws EmfException {
        view.observe(this);

        controlStrategy = service().obtainLocked(session.user(), controlStrategy);
        if (!controlStrategy.isLocked(session.user())) {// view mode, locked by another user
            view.notifyLockFailure(controlStrategy);
            return;
        }

        view.display(controlStrategy);
    }

    public void doClose() throws EmfException {
        service().releaseLocked(controlStrategy);
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public void doSave() throws EmfException {
        saveTabs();
        validateName(controlStrategy);
        
        controlStrategy.setCreator(session.user());
        controlStrategy.setLastModifiedDate(new Date());
        
        service().updateControlStrategy(controlStrategy);
        closeView();
        managerPresenter.doRefresh();
    }
    private void saveTabs() throws EmfException {
        for (Iterator iter = presenters.iterator(); iter.hasNext();) {
            EditControlStrategyTabPresenter element = (EditControlStrategyTabPresenter) iter.next();
            element.doSave();
        }
    }

    private void validateName(ControlStrategy controlStrategy) throws EmfException {
        // emptyName
        String name = controlStrategy.getName();
        if (name.trim().equals(""))
            throw new EmfException("Empty string is not allowed for the name.");

        if (isDuplicate(controlStrategy))
            throw new EmfException("Duplicate name - '" + name + "'.");
    }

    private boolean isDuplicate(ControlStrategy controlStrategy) throws EmfException {
        String name = controlStrategy.getName();
        ControlStrategy[] controlStrategies = service().getControlStrategies();

        for (int i = 0; i < controlStrategies.length; i++) {

            if (controlStrategies[i].getName().equals(name) && controlStrategies[i].getId() != controlStrategy.getId())
                return true;
        }
        return false;
    }

    private ControlStrategyService service() {
        return session.controlStrategyService();
    }

    public void set(EditControlStrategySummaryTabView view) {
        EditControlStrategySummaryTabPresenter presenter = new EditControlStrategySummaryTabPresenterImpl(controlStrategy,view);
        presenters.add(presenter);
    }

}
