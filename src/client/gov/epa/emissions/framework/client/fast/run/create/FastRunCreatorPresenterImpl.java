package gov.epa.emissions.framework.client.fast.run.create;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.fast.run.FastRunManagerPresenter;
import gov.epa.emissions.framework.client.fast.run.FastRunPresenter;
import gov.epa.emissions.framework.client.fast.run.FastRunView;
import gov.epa.emissions.framework.client.fast.run.tabs.FastRunTabPresenter;
import gov.epa.emissions.framework.client.fast.run.tabs.FastRunTabPresenterImpl;
import gov.epa.emissions.framework.client.fast.run.tabs.FastRunTabView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.fast.FastRun;
import gov.epa.emissions.framework.services.fast.FastService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FastRunCreatorPresenterImpl implements FastRunPresenter {

    private EmfSession session;

    private FastRunView view;

    private FastRun run;

    private List<FastRunTabPresenter> presenters;

    // private boolean inputsLoaded = false;

    private boolean hasResults = false;

    // private FastManagerPresenter fastManagerPresenter;

    public FastRunCreatorPresenterImpl(EmfSession session, FastRunView view,
            FastRunManagerPresenter fastManagerPresenter) {

        this.session = session;
        this.view = view;
        this.presenters = new ArrayList<FastRunTabPresenter>();
        // this.fastManagerPresenter = fastManagerPresenter;
    }

    public FastRunCreatorPresenterImpl(EmfSession session, FastRunView view) {

        this.session = session;
        this.view = view;
        this.presenters = new ArrayList<FastRunTabPresenter>();
    }

    public void addTab(FastRunTabView view) {

        FastRunTabPresenter tabPresenter = new FastRunTabPresenterImpl(view);
        tabPresenter.doDisplay();
        this.presenters.add(tabPresenter);
    }

    public void doDisplay() {

        this.view.observe(this);

        this.run = new FastRun();
        this.run.setLastModifiedDate(new Date());
        this.run.setRunStatus("Not started");
        this.run.setCreator(this.session.user());
        this.view.display(this.run);
    }

    public void doClose() throws EmfException {

        /*
         * only release if its an existing program
         */
        if (this.run.getId() != 0) {
            this.getService().releaseLockedFastRun(this.session.user(), this.run.getId());
        }

        this.closeView();
    }

    public void doRun() throws EmfException {
        
        this.doSave();
        this.getService().runFastRun(this.session.user(), this.run.getId());
    }

    public void doRefresh() throws EmfException {
        this.refreshTabs();
    }

    protected void refreshTabs() {

        if (false) {
            throw new RuntimeException("asdf asdfasdf asdfasdf a");
        }

        for (FastRunTabPresenter presenter : this.presenters) {
            presenter.doRefresh(this.run);
        }
    }

    private void closeView() {
        this.view.disposeView();
    }

    public void doSave() throws EmfException {

        this.saveTabs();

        int id = this.run.getId();

        /*
         * update if it's an existing fast run
         */
        if (id != 0) {
            this.getService().updateFastRunWithLock(this.run);
        } else {

            /*
             * add if it's not an existing fast run
             */
            id = getService().addFastRun(this.run);

            /*
             * now lock this fast run, so it can be further edited...
             */
            this.run = getService().obtainLockedFastRun(session.user(), id);
        }
    }

    protected void saveTabs() throws EmfException {

        for (FastRunTabPresenter presenter : this.presenters) {
            presenter.doSave(this.run);
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
