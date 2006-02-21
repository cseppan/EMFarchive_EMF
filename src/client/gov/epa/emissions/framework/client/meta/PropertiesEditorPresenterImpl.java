package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.ChangeObserver;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.data.Keywords;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabPresenter;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabPresenterImpl;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabView;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTabPresenter;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTabPresenterImpl;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTabView;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabPresenter;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabPresenterImpl;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabView;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.EmfDataset;

public class PropertiesEditorPresenterImpl implements ChangeObserver, PropertiesEditorPresenter {

    private EmfDataset dataset;

    private DatasetPropertiesEditorView view;

    private EditableSummaryTabPresenter summaryPresenter;

    private boolean unsavedChanges, alert;

    private EditableKeywordsTabPresenter keywordsPresenter;

    private EmfSession session;

    private EditNotesTabPresenter notesPresenter;

    public PropertiesEditorPresenterImpl(EmfDataset dataset, DatasetPropertiesEditorView view, EmfSession session) {
        this.dataset = dataset;
        this.session = session;
        this.view = view;
        this.alert = false;
    }

    public void doDisplay() throws EmfException {
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

    public void doSave() throws EmfException {
        save(dataService(), summaryPresenter, keywordsPresenter, notesPresenter);
    }

    void save(DataService service, EditableSummaryTabPresenter summary, EditableKeywordsTabPresenter keywords,
            EditNotesTabPresenter notes) throws EmfException {
        updateDataset(service, summary, keywords, notes);

        if(!alert)
            view.close();
    }

    private DataService dataService() {
        return session.dataService();
    }

    void updateDataset(DataService service, EditableSummaryTabPresenter summary, EditableKeywordsTabPresenter keywords,
            EditNotesTabPresenter notes) throws EmfException {
        summary.doSave();
        keywords.doSave();
        notes.doSave();
        if(!alert)
            service.updateDataset(dataset);
    }

    public void set(EditableSummaryTabView summary) {
        summaryPresenter = new EditableSummaryTabPresenterImpl(dataset, summary);
        summary.observeChanges(this);
    }

    public void set(EditableKeywordsTabView keywordsView) throws EmfException {
        keywordsPresenter = new EditableKeywordsTabPresenterImpl(keywordsView, dataset);

        Keywords keywords = new Keywords(session.dataCommonsService().getKeywords());
        keywordsPresenter.display(keywords);
        keywordsView.observeChanges(this);
    }

    public void set(EditNotesTabView view) throws EmfException {
        notesPresenter = new EditNotesTabPresenterImpl(dataset, session, view);
        notesPresenter.display();
    }

    public void onChange() {
        unsavedChanges = true;
    }
    
    public void alert(boolean alert){
        this.alert = alert;
    }

}
