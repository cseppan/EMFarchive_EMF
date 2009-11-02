package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class EditorControlMeasurePresenterImpl implements ControlMeasurePresenter {

    protected ControlMeasure measure;

    protected ControlMeasureView view;

    private List presenters;

    protected EmfSession session;

    // private RefreshObserver parent;

    private ControlMeasureSccTabView sccTabView;
    
    private ControlMeasureTabView summaryTabView;

    public EditorControlMeasurePresenterImpl(ControlMeasure measure, ControlMeasureView view, EmfSession session,
            RefreshObserver parent) {
        this.measure = measure;
        this.view = view;
        this.session = session;
        // this.parent = parent;
        presenters = new ArrayList();
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        
        //make sure the editor is EITHER the admin or creator of the measure...
        //need to load a full object for this check, the initialized measure is light in scope
        measure = session.controlMeasureService().getMeasure(measure.getId());
        if (!measure.getCreator().equals(session.user()) && !session.user().isAdmin()) {
            view.notifyEditFailure(measure);
            return;
        }

        measure = session.controlMeasureService().obtainLockedMeasure(session.user(), measure.getId());
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
        session.controlMeasureService().releaseLockedControlMeasure(session.user(), measure.getId());
        try {
            view.disposeView();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void doSave() throws EmfException {
        save(measure, session.controlMeasureService(), presenters, view);
    }

    void save(ControlMeasure measure, ControlMeasureService service, List presenters, ControlMeasureView view)
            throws EmfException {
        for (Iterator iter = presenters.iterator(); iter.hasNext();) {
            ControlMeasureTabPresenter element = (ControlMeasureTabPresenter) iter.next();
            element.doSave(measure);
        }

        service.updateMeasure(measure, sccTabView.sccs());

        view.disposeView();
        // parent.doRefresh();
    }

    public void set(ControlMeasureSummaryTab summary) {
        this.summaryTabView = summary;
        ControlMeasureTabPresenterImpl tabPresenter = new ControlMeasureTabPresenterImpl(summary);
        presenters.add(tabPresenter);
    }

    public void set(ControlMeasureEfficiencyTabView effTabView) {
        EditableCMEfficiencyTabPresenterImpl effTabPresenter = new EditableCMEfficiencyTabPresenterImpl(effTabView);
        presenters.add(effTabPresenter);
    }

    public void set(ControlMeasureSccTabView sccTabView) {
        this.sccTabView = sccTabView;
        ControlMeasureTabPresenterImpl sccPresenter = new ControlMeasureTabPresenterImpl(sccTabView);
        presenters.add(sccPresenter);
    }
    
    public void set(ControlMeasureEquationTab equationTabView) {
        ControlMeasureTabPresenterImpl equationPresenter = new ControlMeasureTabPresenterImpl(equationTabView);
        presenters.add(equationPresenter);
    }

    public void set(ControlMeasurePropertyTab propertyTabView) {
        ControlMeasureTabPresenterImpl propertyPresenter = new ControlMeasureTabPresenterImpl(propertyTabView);
        presenters.add(propertyPresenter);
    }

    public void set(ControlMeasureReferencesTab referencesTabView) {

        ControlMeasureTabPresenterImpl propertyPresenter = new ControlMeasureTabPresenterImpl(referencesTabView);
        presenters.add(propertyPresenter);
    }

    public void doRefresh(ControlMeasure controlMeasure) {
        this.measure = controlMeasure;
    }

    public void doModify() {
        measure.setLastModifiedBy(session.user().getName());
        measure.setLastModifiedTime(new Date());
        summaryTabView.modify();
    }

    public void fireTracking() {
        view.signalChanges();
    }

    public Pollutant[] getPollutants() throws EmfException {
        return session.dataCommonsService().getPollutants();
    }

}
