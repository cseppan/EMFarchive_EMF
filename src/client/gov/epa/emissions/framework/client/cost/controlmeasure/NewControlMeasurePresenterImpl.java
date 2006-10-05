package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlMeasureService;
import gov.epa.emissions.framework.ui.RefreshObserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NewControlMeasurePresenterImpl implements ControlMeasurePresenter {

    private ControlMeasure measure;

    private ControlMeasureView view;

    private List presenters;

    private EmfSession session;

    private RefreshObserver parent;

    private EditableCMSCCTab sccView;

    public NewControlMeasurePresenterImpl(ControlMeasure measure, ControlMeasureView view, EmfSession session,
            RefreshObserver parent) {
        this.measure = measure;
        this.view = view;
        this.session = session;
        this.parent = parent;
        presenters = new ArrayList();
    }

    public void doDisplay() {
        view.observe(this);
        view.display(measure);
    }

    public void doClose() {
        view.disposeView();
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

        service.addMeasure(measure, sccView.sccs());
        view.disposeView();
        parent.doRefresh();
    }

    public void set(ControlMeasureTabView summary) {
        EditableCMSummaryTabPresenterImpl summaryPresenter = new EditableCMSummaryTabPresenterImpl(summary);
        presenters.add(summaryPresenter);
    }

    public void set(ControlMeasureEfficiencyTabView effTabView) {
        EditableCMEfficiencyTabPresenterImpl effTabPresenter = new EditableCMEfficiencyTabPresenterImpl(effTabView);
        presenters.add(effTabPresenter);
    }
    
    public void set(EditableCMSCCTab sccView) {
        this.sccView = sccView;
        EditableCMSCCTabPresenterImpl sccPresenter = new EditableCMSCCTabPresenterImpl(sccView);
        presenters.add(sccPresenter);
    }

    public void set(ControlMeasureSccTabView effTabView) {
        // NOTE Auto-generated method stub
        
    }


}
