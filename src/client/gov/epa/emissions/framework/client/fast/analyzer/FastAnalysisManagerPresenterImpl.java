package gov.epa.emissions.framework.client.fast.analyzer;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.services.fast.FastService;
import gov.epa.emissions.framework.ui.RefreshObserver;

public class FastAnalysisManagerPresenterImpl implements RefreshObserver, FastAnalysisManagerPresenter {

    private FastAnalysisManagerView view;

    private EmfSession session;

    private FastAnalysis[] analyses = new FastAnalysis[0];

    public FastAnalysisManagerPresenterImpl(EmfSession session, FastAnalysisManagerView view) {

        this.session = session;
        this.view = view;
    }

    public void display() throws EmfException {

        view.display(service().getFastAnalyses());
        view.observe(this);
    }

    private FastService service() {
        return session.fastService();
    }

    public void doRefresh() throws EmfException {
        view.refresh(service().getFastAnalyses());
    }

    public void doClose() {
    }

    public void doNew() throws EmfException {

        throw new EmfException("New not implemented.");

        // FastView creatorView = new FastCreatorWindow(this.view.getDesktopManager(), session, this.view
        // .getParentConsole());
        // FastPresenter presenter = new FastCreatorPresenterImpl(session, creatorView, this);
        // presenter.doDisplay();
    }

    public void doView(int id) throws EmfException {

        throw new EmfException("View not implemented.");
    }

    public void doEdit(int id) throws EmfException {

        throw new EmfException("Edit not implemented.");

        // FastView editorView = new FastEditorWindow(this.view.getDesktopManager(), session,
        // this.view.getParentConsole());
        // FastPresenter presenter = new FastEditorPresenterImpl(id, session, editorView, this);
        // presenter.doDisplay();
    }

    public void doRemove(int[] ids) throws EmfException {
        service().removeFastAnalyses(ids, this.session.user());
    }

    public void doSaveCopiedAnalysis(int id, User creator) throws EmfException {
        service().copyFastAnalysis(id, this.session.user());
    }

    public void doAnalysis(int[] id) throws EmfException {
        throw new EmfException("Analysis not implemented.");
    }

    public void doExport(int[] id) throws EmfException {
        throw new EmfException("Export not implemented.");
    }

    public FastAnalysis[] getAnalyses() {
        return this.analyses;
    }

    public void loadAnalyses() throws EmfException {
        this.analyses = session.fastService().getFastAnalyses();
    }
}
