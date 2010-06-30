package gov.epa.emissions.framework.client.fast.analyzer.create;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.fast.MPSDTManagerPresenter;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisPresenter;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisTabView;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisView;
import gov.epa.emissions.framework.client.fast.analyzer.tabs.FastAnalysisTabPresenterImpl;
import gov.epa.emissions.framework.client.fast.analyzer.tabs.FastAnalysisTabPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.services.fast.FastService;

import java.util.ArrayList;
import java.util.List;

public class FastAnalysisCreatorPresenterImpl implements FastAnalysisPresenter {

    private EmfSession session;

    private FastAnalysisView view;

    private FastAnalysis analysis;

    private List<FastAnalysisTabPresenter> presenters;

    // private boolean inputsLoaded = false;

    private boolean hasResults = false;

    // private FastManagerPresenter fastManagerPresenter;

    public FastAnalysisCreatorPresenterImpl(EmfSession session, FastAnalysisView view,
            MPSDTManagerPresenter fastManagerPresenter) {

        this.session = session;
        this.view = view;
        this.presenters = new ArrayList<FastAnalysisTabPresenter>();
        // this.fastManagerPresenter = fastManagerPresenter;
    }

    public FastAnalysisCreatorPresenterImpl(EmfSession session, FastAnalysisView view) {

        this.session = session;
        this.view = view;
        this.presenters = new ArrayList<FastAnalysisTabPresenter>();
    }

    public void addTab(FastAnalysisTabView view) {

        FastAnalysisTabPresenter tabPresenter = new FastAnalysisTabPresenterImpl(view);
        tabPresenter.doDisplay();
        this.presenters.add(tabPresenter);
    }

    public void doDisplay() {

        this.view.observe(this);

        this.analysis = new FastAnalysis();
        this.view.display(this.analysis);
    }

    public void doClose() throws EmfException {

        /*
         * only release if its an existing program
         */
        if (this.analysis.getId() != 0) {
            this.getService().releaseLockedFastAnalysis(this.session.user(), this.analysis.getId());
        }

        this.closeView();
    }

    private void closeView() {
        this.view.disposeView();
    }

    public void doSave() throws EmfException {

        this.saveTabs();
        int id = getService().addFastAnalysis(this.analysis);

        /*
         * now lock this control program, so it can be further edited...
         */
        this.analysis = getService().obtainLockedFastAnalysis(session.user(), id);
    }

    protected void saveTabs() throws EmfException {

        for (FastAnalysisTabPresenter presenter : this.presenters) {
            presenter.doSave(this.analysis);
        }
    }

    private FastService getService() {
        return this.session.fastService();
    }

    public void fireTracking() {
        view.signalChanges();
    }

    public boolean hasResults() {
        return this.hasResults;
    }
}
