package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlstrategy.ControlStrategiesManagerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;

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

    private EditControlStrategySummaryTabView summaryTabView;

    private EditControlStrategySummaryTabPresenter summaryTabPresenter;

    private EditControlStrategyMeasuresTabPresenter measuresTabPresenter;

    private EditControlStrategyPollutantsTabPresenter pollutantsTabPresenter;

    private EditControlStrategyConstraintsTabPresenter constraintsTabPresenter;
    
    private boolean inputsLoaded = false;
    
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
        ControlStrategyResult[] controlStrategyResults = getResult();
        view.display(controlStrategy, controlStrategyResults);
    }

    private ControlStrategyResult[] getResult() throws EmfException {
        return service().getControlStrategyResults(controlStrategy.getId());
    }

    public void doClose() throws EmfException {
        service().releaseLocked(controlStrategy.getId());
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
        controlStrategy = service().updateControlStrategyWithLock(controlStrategy);
//        managerPresenter.doRefresh();
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
            throw new EmfException("A Control Strategy named '" + name + "' already exists.");
    }

    private boolean isDuplicate(ControlStrategy controlStrategy) throws EmfException {
        int id = service().isDuplicateName(controlStrategy.getName());
        return (id != 0 && controlStrategy.getId() != id);
//        String name = controlStrategy.getName();
//        ControlStrategy[] controlStrategies = service().getControlStrategies();
//
//        for (int i = 0; i < controlStrategies.length; i++) {
//
//            if (controlStrategies[i].getName().equals(name) && controlStrategies[i].getId() != controlStrategy.getId())
//                return true;
//        }
//        return false;
    }

    private ControlStrategyService service() {
        return session.controlStrategyService();
    }

    public void set(EditControlStrategySummaryTabView view) {
        this.summaryTabView = view;
        this.summaryTabPresenter = new EditControlStrategySummaryTabPresenterImpl(controlStrategy, view);
        presenters.add(summaryTabPresenter);
    }

    public void set(EditControlStrategyOutputTabView view) {
        EditControlStrategyOutputTabPresenter presenter = new EditControlStrategyOutputTabPresenter(session, view);
        presenter.doDisplay();
        presenters.add(presenter);
    }

    public void set(EditControlStrategyTabView view) {
        EditControlStrategyTabPresenter presenter = new EditControlStrategyTabPresenterImpl(controlStrategy, view);
        presenters.add(presenter);
    }

    public void setResults(ControlStrategy controlStrategy) {
        summaryTabView.setRunMessage(controlStrategy);
    }

    public void stopRun() throws EmfException {
        service().stopRunStrategy();
        summaryTabView.stopRun();
    }

    public void runStrategy() throws EmfException {
        service().runStrategy(session.user(), controlStrategy);
    }

    public void doRefresh() throws EmfException {
        //ControlStrategyResult result = session.controlStrategyService().controlStrategyResults(controlStrategy);
        ControlStrategyResult[] controlStrategyResults = getResult();
//        String runStatus = service().controlStrategyRunStatus(controlStrategy.getId());
//        if (runStatus == null || !runStatus.equalsIgnoreCase("Running")) {
            for (Iterator iter = presenters.iterator(); iter.hasNext();) {
                EditControlStrategyTabPresenter element = (EditControlStrategyTabPresenter) iter.next();
                element.doRefresh(controlStrategyResults);
            }
//        }
    }

    public void set(ControlStrategyMeasuresTabView view) {
        measuresTabPresenter = new EditControlStrategyMeasuresTabPresenter(view,
                controlStrategy, session, 
                managerPresenter);
        presenters.add(measuresTabPresenter);
    }

    public void set(ControlStrategyPollutantsTabView view) {
        pollutantsTabPresenter = new EditControlStrategyPollutantsTabPresenter(view,
                controlStrategy, session);
        pollutantsTabPresenter.doDisplay();
        presenters.add(pollutantsTabPresenter);
    }

    public void set(ControlStrategyConstraintsTabView view) {
        constraintsTabPresenter = new EditControlStrategyConstraintsTabPresenter(view,
                controlStrategy, session);
        constraintsTabPresenter.doDisplay();
        presenters.add(constraintsTabPresenter);
    }

    public void doLoad(String tabTitle) throws EmfException {
        if (!inputsLoaded && tabTitle.equalsIgnoreCase("Measures")) {
            measuresTabPresenter.doDisplay();
            inputsLoaded = true;
        }
    }
    
    public CostYearTable getCostYearTable() throws EmfException {
        return session.controlMeasureService().getCostYearTable(1999);
    }

    public void fireTracking() {
        view.signalChanges();
    }
}
