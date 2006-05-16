package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.CostService;
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

    public void doClose() throws EmfException {
        view.disposeView();
        parent.doRefresh();
    }

    public void doSave() throws EmfException {
        save(measure, session.costService(), presenters, view);
    }

    void save(ControlMeasure measure, CostService service, List presenters, ControlMeasureView view)
            throws EmfException {
        for (Iterator iter = presenters.iterator(); iter.hasNext();) {
            ControlMeasureTabPresenter element = (ControlMeasureTabPresenter) iter.next();
            element.doSave();
        }

        service.addMeasure(measure);
        view.disposeView();
        parent.doRefresh();
    }

    public void set(EditableCMSummaryTabView summary) {
        EditableCMSummaryTabPresenterImpl summaryPresenter = new EditableCMSummaryTabPresenterImpl(measure, summary);
        presenters.add(summaryPresenter);
    }


}
