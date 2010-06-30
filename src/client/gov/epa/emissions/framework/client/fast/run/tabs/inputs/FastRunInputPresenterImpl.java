package gov.epa.emissions.framework.client.fast.run.tabs.inputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastRun;
import gov.epa.emissions.framework.services.fast.FastRunInput;
import gov.epa.emissions.framework.services.fast.FastService;

public class FastRunInputPresenterImpl implements FastRunInputPresenter {

    private EmfSession session;

    private FastRunInputView view;

    private FastRunInput input;

    private int id;

    private boolean hasResults = false;

    public FastRunInputPresenterImpl(int id, EmfSession session, FastRunInputView view) {

        this.id = id;
        this.session = session;
        this.view = view;
    }

    public void doDisplay() throws EmfException {

        // this.view.observe(this);
        //
        // this.input = this.getService().obtainLockedRunInput(this.session.user(), this.id);
        //
        // if (!this.input.isLocked(this.session.user())) {
        // this.view.notifyLockFailure(this.input);
        // } else {
        // this.view.display(this.input);
        // }
    }

    public void doCreate() {

        view.observe(this);
        view.display(this.input);
    }

    public FastRun getEntity(int id) throws EmfException {
        return this.getService().getFastRun(id);
    }

    public void doClose() throws EmfException {

        // /*
        // * only release if its an existing program
        // */
        // if (this.input.getId() != 0) {
        // this.getService().releaseLockedInput(this.session.user(), this.input.getId());
        // }
        //
        // this.closeView();
    }

    private void closeView() {
        this.view.disposeView();
    }

    public void doSave() throws EmfException {

        // this.saveInput();
        // this.input = getService().updateRunInputWithLock(this.session.user(), this.input);
    }

    protected void saveInput() throws EmfException {
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
