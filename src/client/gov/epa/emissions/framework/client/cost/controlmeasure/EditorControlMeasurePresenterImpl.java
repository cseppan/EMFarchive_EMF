package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EditorControlMeasurePresenterImpl implements ControlMeasurePresenter {

    private ControlMeasure measure;

    private ControlMeasureView view;

    private List presenters;

    private EmfSession session;

    private RefreshObserver parent;

    public EditorControlMeasurePresenterImpl(ControlMeasure measure, ControlMeasureView view, EmfSession session,
            RefreshObserver parent) {
        this.measure = measure;
        this.view = view;
        this.session = session;
        this.parent = parent;
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

    public void doClose() throws EmfException {
        session.costService().releaseLockedControlMeasure(measure);
        view.disposeView();
        parent.doRefresh();
    }
    
    public void doSave() throws EmfException {
        save(measure, session.costService(), presenters, view);
    }

    void save(ControlMeasure measure, ControlMeasureService service, List presenters, ControlMeasureView view)
            throws EmfException {
        for (Iterator iter = presenters.iterator(); iter.hasNext();) {
            ControlMeasureTabPresenter element = (ControlMeasureTabPresenter) iter.next();
            element.doSave(measure);
        }

        service.updateMeasure(measure);

        view.disposeView();
        parent.doRefresh();
    }

    public void set(EditableCMTabView summary) {
        ControlMeasureTabPresenterImpl tabPresenter = new ControlMeasureTabPresenterImpl(summary);
        presenters.add(tabPresenter);
    }

    public void set(EditableCostsTabView costTabView) {
        EditableCMCostTabPresenterImpl costTabPresenter = new EditableCMCostTabPresenterImpl(costTabView);
        presenters.add(costTabPresenter);
    }

    public void set(EditableEfficiencyTabView effTabView) {
        EditableCMEfficiencyTabPresenterImpl effTabPresenter = new EditableCMEfficiencyTabPresenterImpl(effTabView);
        presenters.add(effTabPresenter);
    }

}
