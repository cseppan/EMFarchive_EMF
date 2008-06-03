package gov.epa.emissions.framework.client.cost.controlstrategy.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlstrategy.ControlStrategiesManagerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyService;
import gov.epa.emissions.framework.services.cost.StrategyType;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.services.data.EmfDataset;

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
    
    private EditControlStrategyTabPresenter inventoryTabPresenter;
    
    private EditControlStrategySummaryTabPresenter summaryTabPresenter;

    private EditControlStrategyMeasuresTabPresenter measuresTabPresenter;

    private ControlStrategyProgramsTabPresenter programsTabPresenter;

    private EditControlStrategyPollutantsTabPresenter pollutantsTabPresenter;

    private EditControlStrategyConstraintsTabPresenter constraintsTabPresenter;
    
    private boolean inputsLoaded = false;
    
    private boolean hasResults = false;
    
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
        
        controlStrategy = service().obtainLocked(session.user(), controlStrategy.getId());
        
        if (!controlStrategy.isLocked(session.user())) {// view mode, locked by another user
            view.notifyLockFailure(controlStrategy);
            return;
        }
        ControlStrategyResult[] controlStrategyResults = getResult();
        if (controlStrategyResults!= null && controlStrategyResults.length > 0) hasResults = true;
        view.display(controlStrategy, controlStrategyResults);
    }

    private ControlStrategyResult[] getResult() throws EmfException {
        return service().getControlStrategyResults(controlStrategy.getId());
    }

    public ControlStrategy getControlStrategy(int id) throws EmfException {
        return service().getById(id);
    }

    public void doClose() throws EmfException {
        service().releaseLocked(session.user(), controlStrategy.getId());
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
            element.doSave(controlStrategy);
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
        this.summaryTabPresenter = new EditControlStrategySummaryTabPresenterImpl(this, controlStrategy, view);
        presenters.add(summaryTabPresenter);
    }

    public void set(EditControlStrategyOutputTabView view) throws EmfException {
        EditControlStrategyOutputTabPresenter presenter = new EditControlStrategyOutputTabPresenter(session, view);
        presenter.doDisplay(controlStrategy, getResult());
        presenters.add(presenter);
    }

    public void set(EditControlStrategyTabView view) {
        this.inventoryTabPresenter = new EditControlStrategyTabPresenterImpl(controlStrategy, view);
        presenters.add(this.inventoryTabPresenter);
    }

    public void setResults(ControlStrategy controlStrategy) {
        summaryTabView.setRunMessage(controlStrategy);
    }

    public void stopRun() throws EmfException {
        service().stopRunStrategy(controlStrategy.getId());
        view.stopRun();
    }

    public void runStrategy() throws EmfException {
        service().runStrategy(session.user(), controlStrategy.getId());
    }

    public void doRefresh() throws EmfException {
        //ControlStrategyResult result = session.controlStrategyService().controlStrategyResults(controlStrategy);
        ControlStrategyResult[] controlStrategyResults = getResult();
        ControlStrategy strategy = getControlStrategy(controlStrategy.getId());
//        String runStatus = service().controlStrategyRunStatus(controlStrategy.getId());
//        if (runStatus == null || !runStatus.equalsIgnoreCase("Running")) {
            for (Iterator iter = presenters.iterator(); iter.hasNext();) {
                EditControlStrategyTabPresenter element = (EditControlStrategyTabPresenter) iter.next();
                element.doRefresh(strategy, controlStrategyResults);
            }
//        }
    }

    public void set(ControlStrategyMeasuresTabView view) {
        measuresTabPresenter = new EditControlStrategyMeasuresTabPresenter(view,
                controlStrategy, session, 
                managerPresenter);
        presenters.add(measuresTabPresenter);
    }

    public void set(ControlStrategyProgramsTab view) {
        programsTabPresenter = new ControlStrategyProgramsTabPresenter(view,
                controlStrategy, session);
        programsTabPresenter.doDisplay();
        presenters.add(programsTabPresenter);
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
        return session.controlMeasureService().getCostYearTable(CostYearTable.REFERENCE_COST_YEAR);
    }

    public void fireTracking() {
        view.signalChanges();
    }

    public DatasetType getDatasetType(String name) throws EmfException {
        return session.dataCommonsService().getDatasetType(name);
    }
    
    public Version[] getVersions(EmfDataset dataset) throws EmfException 
    {
        if (dataset == null) {
            return new Version[0];
        }
        return session.dataEditorService().getVersions(dataset.getId());
    }

    public EmfDataset[] getDatasets(DatasetType type) throws EmfException
    {
            if (type == null)
                return new EmfDataset[0];

            return session.dataService().getDatasets(type);
    }

    public boolean hasResults() {
        return this.hasResults;
    }
    
    public EmfDataset getDataset(int id) throws EmfException {
        return session.dataService().getDataset(id);
    }

    public void doChangeStrategyType(StrategyType strategyType) {
        if (constraintsTabPresenter != null)
            constraintsTabPresenter.doChangeStrategyType(strategyType);
        if (inventoryTabPresenter != null)
            inventoryTabPresenter.doChangeStrategyType(strategyType);
        view.notifyStrategyTypeChange(strategyType);
    }

}
