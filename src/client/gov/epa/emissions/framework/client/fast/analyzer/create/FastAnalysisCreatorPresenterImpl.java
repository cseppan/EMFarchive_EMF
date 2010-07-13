package gov.epa.emissions.framework.client.fast.analyzer.create;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisManagerPresenter;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisPresenter;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisTabView;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisView;
import gov.epa.emissions.framework.client.fast.analyzer.tabs.FastAnalysisTabPresenter;
import gov.epa.emissions.framework.client.fast.analyzer.tabs.FastAnalysisTabPresenterImpl;
import gov.epa.emissions.framework.client.fast.run.tabs.FastRunTabPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.services.fast.FastService;

import java.util.ArrayList;
import java.util.Date;
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
            FastAnalysisManagerPresenter fastManagerPresenter) {

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

    public void doRun() throws EmfException {

        this.doSave();
        this.getService().runFastAnalysis(this.session.user(), this.analysis.getId());
    }

    public void doRefresh() throws EmfException {
        this.refreshTabs();
    }

    protected void refreshTabs() {

        if (false) {
            throw new RuntimeException("asdf asdfasdf asdfasdf a");
        }

        for (FastAnalysisTabPresenter presenter : this.presenters) {
            presenter.doRefresh(this.analysis);
        }
    }

    public void doDisplay() {

        this.view.observe(this);

        this.analysis = new FastAnalysis();
        this.analysis.setCreator(this.session.user());
        this.analysis.setLastModifiedDate(new Date());
        this.analysis.setRunStatus("Not started");

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

        if (false) {
            throw new RuntimeException("oiasdf");
        }

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

    public DatasetType getDatasetType(String name) throws EmfException {
        return session.dataCommonsService().getDatasetType(name);
    }

    public Version[] getVersions(EmfDataset dataset) throws EmfException {

        Version[] versions = new Version[0];

        if (dataset != null) {
            versions = this.session.dataEditorService().getVersions(dataset.getId());
        }

        return versions;
    }
}
