package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.CostService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlMeasuresEditorPresenterImpl implements ControlMeasuresEditorPresenter {

    private ControlMeasure measure;

    private ControlMeasuresEditorView view;

    private List presenters;
    
    private EmfSession session;

    public ControlMeasuresEditorPresenterImpl(ControlMeasure measure, ControlMeasuresEditorView view, EmfSession session) {
        this.measure = measure;
        this.view = view;
        this.session = session;
        presenters = new ArrayList();
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        
        measure = session.costService().obtainLockedMeasure(session.user(), measure);
        if (!measure.isLocked(session.user())) {// view mode, locked by another user
            view.notifyLockFailure(measure);
            return;
        }
        
        display();
    }

    void display() {
        view.display(measure);
    }

    public void doClose() {
        view.disposeView();
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
        service.addMeasure(measure);

        view.disposeView();
    }

    public void set(EditableCMSummaryTabView summary) {
        EditableCMSummaryTabPresenterImpl summaryPresenter = new EditableCMSummaryTabPresenterImpl(measure, summary);
        presenters.add(summaryPresenter);
    }

}
