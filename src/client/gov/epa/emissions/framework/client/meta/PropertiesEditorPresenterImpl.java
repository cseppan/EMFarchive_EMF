package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.ChangeObserver;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.data.Browser;
import gov.epa.emissions.framework.client.data.Keywords;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabPresenter;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabView;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTabPresenter;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTabView;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabPresenter;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabView;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.EmfDataset;

public class PropertiesEditorPresenterImpl implements ChangeObserver, PropertiesEditorPresenter {

    private EmfDataset dataset;

    private DatasetPropertiesEditorView view;

    private EditableSummaryTabPresenter summaryPresenter;

    private boolean unsavedChanges;

    private EditableKeywordsTabPresenter keywordsPresenter;

    private EmfSession session;

    private Browser browser;

    public PropertiesEditorPresenterImpl(EmfDataset dataset, EmfSession session, Browser browserPresenter) {
        this.dataset = dataset;
        this.session = session;
        this.browser = browserPresenter;
    }

    public void doDisplay(DatasetPropertiesEditorView view) throws EmfException {
        this.view = view;
        view.observe(this);

        dataset = session.dataService().obtainLockedDataset(session.user(), dataset);

        if (!dataset.isLocked(session.user())) {// view mode, locked by another user
            view.notifyLockFailure(dataset);
            return;
        }

        view.display(dataset);
    }

    public void doClose() throws EmfException {
        if (unsavedChanges && !view.shouldContinueLosingUnsavedChanges())
            return;

        dataService().releaseLockedDataset(dataset);
        view.close();
    }

    public void doSave() {
        DataService service = dataService();
        try {
            updateDataset(service, summaryPresenter, keywordsPresenter);
        } catch (EmfException e) {
            view.showError("Could not save dataset. Reason: " + e.getMessage());
            return;
        }

        view.close();
        browser.notifyUpdates();
    }

    private DataService dataService() {
        return session.dataService();
    }

    void updateDataset(DataService service, EditableSummaryTabPresenter summary, EditableKeywordsTabPresenter keywords)
            throws EmfException {
        summary.doSave();
        keywords.doSave();
        service.updateDataset(dataset);
    }

    public void set(EditableSummaryTabView summary) {
        summaryPresenter = new EditableSummaryTabPresenter(dataset, summary);
        summary.observeChanges(this);
    }

    public void set(EditableKeywordsTabView keywordsView) throws EmfException {
        keywordsPresenter = new EditableKeywordsTabPresenter(keywordsView, dataset);

        Keywords keywords = new Keywords(session.dataCommonsService().getKeywords());
        keywordsPresenter.display(keywords);
    }

    public void onChange() {
        unsavedChanges = true;
    }

    public void set(EditNotesTabView view) throws EmfException {
        EditNotesTabPresenter presenter = new EditNotesTabPresenter(dataset, session.dataCommonsService(), view);
        presenter.display();
    }

}
