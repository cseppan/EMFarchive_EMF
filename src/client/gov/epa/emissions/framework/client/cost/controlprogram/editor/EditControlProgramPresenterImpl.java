package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.cost.controlprogram.ControlProgramManagerPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.services.cost.ControlProgramService;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class EditControlProgramPresenterImpl implements EditControlProgramPresenter {

    private EmfSession session;

    private EditControlProgramView view;

    private ControlProgramManagerPresenter managerPresenter;

    private ControlProgram controlProgram;

    private List presenters;

    private EditControlProgramMeasuresTabPresenter measuresTabPresenter;

    private EditControlProgramSummaryTabPresenter summaryTabPresenter;

//    private EditControlProgramMeasuresTabPresenter measuresTabPresenter;
//
//    private boolean inputsLoaded = false;
    
    private boolean hasResults = false;
    
    public EditControlProgramPresenterImpl(ControlProgram controlProgram, EmfSession session, 
            EditControlProgramView view, ControlProgramManagerPresenter controlProgramManagerPresenter) {
        this.controlProgram = controlProgram;
        this.session = session;
        this.view = view;
        this.managerPresenter = controlProgramManagerPresenter;
        this.presenters = new ArrayList();
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        
        controlProgram = service().obtainLocked(session.user(), controlProgram.getId());
        
        if (!controlProgram.isLocked(session.user())) {// view mode, locked by another user
            view.notifyLockFailure(controlProgram);
            return;
        }
        view.display(controlProgram);
    }

    public ControlProgram getControlProgram(int id) throws EmfException {
        return service().getControlProgram(id);
    }

    public void doClose() throws EmfException {
        service().releaseLocked(session.user(), controlProgram.getId());
        closeView();
    }

    private void closeView() {
        view.disposeView();
    }

    public void doSave() throws EmfException {
        saveTabs();
        controlProgram.setCreator(session.user());
        controlProgram.setLastModifiedDate(new Date());
        controlProgram = service().updateControlProgramWithLock(controlProgram);
//        managerPresenter.doRefresh();
    }

    private void saveTabs() throws EmfException {
        for (Iterator iter = presenters.iterator(); iter.hasNext();) {
            EditControlProgramTabPresenter element = (EditControlProgramTabPresenter) iter.next();
            element.doSave(controlProgram);
        }
    }

    private ControlProgramService service() {
        return session.controlProgramService();
    }

    public void set(EditControlProgramSummaryTab view) throws EmfException {
        this.summaryTabPresenter = new EditControlProgramSummaryTabPresenter(view, controlProgram, 
                session, managerPresenter);
        summaryTabPresenter.doDisplay();
        presenters.add(summaryTabPresenter);
    }

    public void set(EditControlProgramMeasuresTab view) throws EmfException {
        measuresTabPresenter = new EditControlProgramMeasuresTabPresenter(view,
                controlProgram, session, 
                managerPresenter);
        measuresTabPresenter.doDisplay();
        presenters.add(measuresTabPresenter);
    }

//    public void doLoad(String tabTitle) throws EmfException {
//        if (!inputsLoaded ) {
//            measuresTabPresenter.doDisplay();
//            summaryTabPresenter.doDisplay();
//            inputsLoaded = true;
//        }
//    }

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

    public void doChangeControlProgramType(ControlProgramType controlProgramType) {
//        if (constraintsTabPresenter != null)
//            constraintsTabPresenter.doChangeStrategyType(strategyType);
//        if (inventoryTabPresenter != null)
//            inventoryTabPresenter.doChangeStrategyType(strategyType);
    }
}
