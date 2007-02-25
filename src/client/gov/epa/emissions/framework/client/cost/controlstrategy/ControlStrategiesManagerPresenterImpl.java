package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyPresenter;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyPresenterImpl;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;
import gov.epa.emissions.framework.services.cost.LightControlMeasure;
import gov.epa.emissions.framework.ui.RefreshObserver;

public class ControlStrategiesManagerPresenterImpl implements RefreshObserver, ControlStrategiesManagerPresenter {
    private ControlStrategyManagerView view;

    private EmfSession session;

    private LightControlMeasure[] controlMeasures = {};

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
        loadControlMeasures();
        view.refresh(service().getControlStrategies());
    }

    public void doClose() {
        view.disposeView();
    }

    public void doNew(ControlStrategyView view) {
        ControlStrategyPresenter presenter = new ControlStrategyPresenterImpl(session, view, this);
        presenter.doDisplay();
        
    }

    public void doEdit(EditControlStrategyView view, ControlStrategy controlStrategy) throws EmfException {
        EditControlStrategyPresenter presenter = new EditControlStrategyPresenterImpl(controlStrategy, session, view, this);
        displayEditor(presenter);
    }

    void displayEditor(EditControlStrategyPresenter presenter) throws EmfException {
        presenter.doDisplay();
    }

//    public void doRemove(ControlStrategy[] strategies) throws EmfException {
//        service().removeControlStrategies(strategies, session.user());
//    }

    public void doRemove(int[] ids) throws EmfException {
        service().removeControlStrategies(ids, session.user());
    }

//    public void doSaveCopiedStrategies(ControlStrategy coppied, String name) throws EmfException {
//        if (isDuplicate(coppied))
//            throw new EmfException("A control strategy named '" + coppied.getName() + "' already exists.");
//
//        coppied.setCreator(session.user());
//        coppied.setLastModifiedDate(new Date());
//        service().addControlStrategy(coppied);
//    }
    
    public void doSaveCopiedStrategies(int id, User creator) throws EmfException {
        service().copyControlStrategy(id, session.user());
    }
    
//    private boolean isDuplicate(ControlStrategy newStrategy) throws EmfException {
//        return (service().isDuplicateName(newStrategy.getName()) != 0);
////        ControlStrategy[] strategies = service().getControlStrategies();
////        for (int i = 0; i < strategies.length; i++) {
////            if (strategies[i].getName().equals(newStrategy.getName()))
////                return true;
////        }
////
////        return false;
//    }

    public LightControlMeasure[] getControlMeasures() {
        return controlMeasures;
    }

    public void loadControlMeasures() throws EmfException  {
        controlMeasures = session.controlMeasureService().getLightControlMeasures();
    }
}
