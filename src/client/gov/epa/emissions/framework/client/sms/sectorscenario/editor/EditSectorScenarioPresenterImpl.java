package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.SectorScenarioService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.sms.SectorScenario;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EditSectorScenarioPresenterImpl  implements EditSectorScenarioPresenter {

    protected EditSectorScenarioView view; 
    protected EditSectorScenarioSummaryTabView summaryTabView;
    private EditSectorScenarioInputsTabView inputsTabView;
    protected EmfSession session;

    //protected SectorScenarioManagerPresenter managerPresenter;
    
    protected List presenters;

    private EditSectorScenarioSummaryTabPresenter summaryTabPresenter;
    
    private EditSectorScenarioInputsTabPresenter inputsTabPresenter;

    protected SectorScenario sectorScenario;
    
    public EditSectorScenarioPresenterImpl(SectorScenario sectorScenario, EmfSession session, 
            EditSectorScenarioView view ) {
        this.session = session;
        this.view = view;
        //this.managerPresenter = managerPresenter;
        this.sectorScenario = sectorScenario;
        this.presenters = new ArrayList();
    }

    public void doDisplay() {
        view.observe(this);

        //  make sure the editor is EITHER the admin or creator   
        
        if (!sectorScenario.isLocked(session.user())) {// view mode, locked by another user
//            view.notifyLockFailure(sectorScenario);
//            return;
        }
        //sectorScenario = session.sectorScenarioService().obtainLocked(session.user(), sectorScenario.getId());
        if (!sectorScenario.isLocked(session.user())) {// view mode, locked by another user
            //view.notifyLockFailure(sectorScenario);
            //return;
        }
        view.display(sectorScenario);
    }

    public void doClose() {
        //service().releaseLocked(session.user(), sectorScenario.getId());
        closeView();
    }
    
    private void closeView() {
        view.disposeView();
    }   

//    public void doRun(ControlStrategy controlStrategy) throws EmfException {
//        saveTabs(controlStrategy);
//        runTabs(controlStrategy);
//    }

//    private void saveTabs(SectorScenario sctorScenario) throws EmfException {
//        for (Iterator iter = presenters.iterator(); iter.hasNext();) {
//            //EditControlStrategyTabPresenter element = (EditControlStrategyTabPresenter) iter.next();
//            //element.doSave(controlStrategy);
//        }
//    }    

    public void set(EditSectorScenarioSummaryTabView view) {
        this.summaryTabView = view;
        this.summaryTabPresenter = new EditSectorScenarioSummaryTabPresenterImpl(session, summaryTabView);
        view.observe(summaryTabPresenter);
        presenters.add(summaryTabPresenter);
    }
    
    public void set(EditSectorScenarioInputsTabView view) {
        this.inputsTabView = view;
        this.inputsTabPresenter = new EditSectorScenarioInputsTabPresenterImpl(session, inputsTabView);
        view.observe(inputsTabPresenter);
        presenters.add(inputsTabPresenter);
    }
    
    
    
   
    public void stopRun() {
        //service().stopRunStrategy(sectorScenario.getId());
        //view.stopRun();
    }

    public void runStrategy() {
        
       // service().runStrategy(session.user(), sectorScenario.getId());
    }

    public void doRefresh() {
        //ControlStrategyResult result = session.controlStrategyService().controlStrategyResults(controlStrategy);
//        ControlStrategyResult[] controlStrategyResults = getResult();
//        ControlStrategy strategy = getControlStrategy(controlStrategy.getId());
//        hasResults = false;
//        if (controlStrategyResults!= null && controlStrategyResults.length > 0) hasResults = true;
//        String runStatus = service().controlStrategyRunStatus(controlStrategy.getId());
//        if (runStatus == null || !runStatus.equalsIgnoreCase("Running")) {
            for (Iterator iter = presenters.iterator(); iter.hasNext();) {
                //EditControlStrategyTabPresenter element = (EditControlStrategyTabPresenter) iter.next();
                //element.doRefresh(strategy);
            }
//        }
    }



//    public void fireTracking() {
//        view.signalChanges();
//    }

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

    public void doLoad(String tabTitle) {
        // NOTE Auto-generated method stub
        
    }
    
    public EmfSession getSession(){
        return session;
    }

    public void doSave() throws EmfException {
        saveTabs();
        validateNameAndAbbre(sectorScenario);
        
        //return service().updateSectorScenario(sectorScenario);
    }
    
    private void saveTabs() throws EmfException {
        for (Iterator iter = presenters.iterator(); iter.hasNext();) {
            EditSectorScenarioTabPresenter element = (EditSectorScenarioTabPresenter) iter.next();
            element.doSave();
        }
    }
    
    private void validateNameAndAbbre(SectorScenario sectorScenario) throws EmfException {
        // emptyName
        String name = sectorScenario.getName();
        String abbre = sectorScenario.getAbbreviation();
        if (name.trim().equals(""))
            throw new EmfException("Empty string is not allowed for the name.");

        if (abbre.trim().equals(""))
            throw new EmfException("Empty string is not allowed for the abbre.");

        if (isDuplicate(name))
            throw new EmfException("A Control Strategy named '" + name + "' already exists.");
    }
    
    private boolean isDuplicate(String name) throws EmfException {
        int id = service().isDuplicateName(name);
        return (id != 0);
    }
    
    private SectorScenarioService service(){    
        return session.sectorScenarioService();
    }

    public Project[] getProjects() throws EmfException {
        return session.dataCommonsService().getProjects();
    }

    public void fireTracking() {
        view.signalChanges();
    }
    
    public EmfDataset getDataset(int id) throws EmfException {
        return session.dataService().getDataset(id);
    }   
}
