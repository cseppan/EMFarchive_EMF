package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.CostService;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlMeasuresEditorPresenterImpl implements ControlMeasuresEditorPresenter {

    private ControlMeasure measure;

    private ControlMeasuresEditorView view;

    private List presenters;
    
    private EmfSession session;
    
    private String newOrEdit;
    
    private RefreshObserver parent;

    public ControlMeasuresEditorPresenterImpl(ControlMeasure measure, ControlMeasuresEditorView view, 
            EmfSession session, RefreshObserver parent) {
        this.measure = measure;
        this.view = view;
        this.session = session;
        this.parent = parent;
        presenters = new ArrayList();
    }

    public void doDisplay(String newOrEdit) throws EmfException {
        view.observe(this);
        this.newOrEdit = newOrEdit;
        
        if(newOrEdit.equalsIgnoreCase("edit"))
            obtainLock();
        
        display();
    }
    
    private void obtainLock() throws EmfException {
        measure = session.costService().obtainLockedMeasure(session.user(), measure);
        if (!measure.isLocked(session.user())) {// view mode, locked by another user
            view.notifyLockFailure(measure);
            return;
        }
    }

    void display() {
        view.display(measure, newOrEdit);
    }

    public void doClose() throws EmfException {
        view.disposeView();
        parent.doRefresh();
    }
    
    public void doSave() throws EmfException {
        save(measure, session.costService(), presenters, view);
    }

    void save(ControlMeasure measure, CostService service, List presenters, ControlMeasuresEditorView view)
            throws EmfException {
        for (Iterator iter = presenters.iterator(); iter.hasNext();) {
            ControlMeasureTabPresenter element = (ControlMeasureTabPresenter) iter.next();
            element.doSave();
        }
        
        if(newOrEdit != null && !newOrEdit.equalsIgnoreCase("edit"))
            service.addMeasure(measure);

        view.disposeView();
        parent.doRefresh();
    }

    public void set(EditableCMSummaryTabView summary) {
        EditableCMSummaryTabPresenterImpl summaryPresenter = new EditableCMSummaryTabPresenterImpl(measure, summary);
        presenters.add(summaryPresenter);
    }

}
