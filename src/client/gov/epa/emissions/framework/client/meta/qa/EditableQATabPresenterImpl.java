package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.qa.QAService;

import java.util.Date;

public class EditableQATabPresenterImpl implements EditableQATabPresenter {

    private EditableQATabView view;

    private EmfDataset dataset;

    private EmfSession session;

    public EditableQATabPresenterImpl(EmfDataset dataset, EmfSession session, EditableQATabView view) {
        this.dataset = dataset;
        this.session = session;
        this.view = view;
        view.observe(this);
    }

    public void display() throws EmfException {
        QAStep[] steps = qaService().getQASteps(dataset);
        QAStepResult[] qaStepResults =qaService().getQAStepResults(dataset);
        view.display(dataset, steps, qaStepResults,  versions());
    }

    private Version[] versions() throws EmfException {
        DataEditorService service = session.dataEditorService();
        return service.getVersions(dataset.getId());
    }

    private QAService qaService() {
        return session.qaService();
    }

    public void doSave() {
        // DO NOTHING
    }
    public void doAddUsingTemplate(NewQAStepView stepView) {
        DatasetType type = dataset.getDatasetType();
        if (type.getQaStepTemplates().length == 0) {
            view.informLackOfTemplatesForAddingNewSteps(type);
            return;
        }

        stepView.display(dataset, type);
        if (stepView.shouldCreate()) {
            view.addFromTemplate(stepView.steps());
        }
    }

    public void doAddCustomized(NewCustomQAStepView stepView) throws EmfException {
        NewCustomQAStepPresenter presenter = new NewCustomQAStepPresenter(stepView, dataset, versions(), view, session);
        doAddCustomized(stepView, presenter);
    }

    void doAddCustomized(NewCustomQAStepView stepView, NewCustomQAStepPresenter presenter) throws EmfException {
        stepView.observe(presenter);
        presenter.display();
    }

    public void doSetStatus(SetQAStatusView statusView, QAStep[] steps) {
        SetQAStatusPresenter presenter = new SetQAStatusPresenter(statusView, steps, view, session);
        presenter.display();
    }

    public synchronized void runStatus(QAStep step) throws EmfException {
        step.setStatus("In Progress");
        step.setDate(new Date());
        step.setWho(session.user().getUsername());
        session.qaService().runQAStep(step, session.user());
        //QAStepResult result = session.qaService().getQAStepResult(step);
        //view.refresh(step, result);
    }

    public void doEdit(QAStep step, EditQAStepView performView, String versionName) throws EmfException {
        EditQAStepPresenter presenter = new EditQAStepPresenter(performView, dataset, view, session);
        presenter.display(step, versionName);
    }

    public void addFromTemplates(QAStep[] newSteps) throws EmfException {
        session.qaService().updateWitoutCheckingConstraints(newSteps);
    }
    
    public EmfSession getSession(){
        return session; 
    }

    public void doCopyQASteps(QAStep[] steps, int[] datasetIds, boolean replace)
            throws EmfException {
        session.qaService().copyQAStepsToDatasets(session.user(), steps, datasetIds, replace);
    }

    public void checkIfLockedByCurrentUser() throws EmfException{
        EmfDataset reloaded = session.dataService().getDataset(dataset.getId());
        if (!reloaded.isLocked())
            throw new EmfException("Lock on current dataset object expired. " );  
        if (!reloaded.isLocked(session.user()))
            throw new EmfException("Lock on current dataset object expired. User " + reloaded.getLockOwner()
                    + " has it now.");    
    }
}
