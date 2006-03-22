package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabPresenter;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabPresenterImpl;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabView;
import gov.epa.emissions.framework.client.meta.keywords.Keywords;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTabPresenter;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTabPresenterImpl;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTabView;
import gov.epa.emissions.framework.client.meta.qa.EditableQAStepsPresenter;
import gov.epa.emissions.framework.client.meta.qa.EditableQAStepsPresenterImpl;
import gov.epa.emissions.framework.client.meta.qa.EditableQATabView;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabPresenter;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabPresenterImpl;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class PropertiesEditorPresenterImpl implements PropertiesEditorPresenter {

    private EmfDataset dataset;

    private DatasetPropertiesEditorView view;

    private EditableSummaryTabPresenter summaryPresenter;

    private EditableKeywordsTabPresenter keywordsPresenter;

    private EditableQAStepsPresenter qaPresenter;

    private EmfSession session;

    private EditNotesTabPresenter notesPresenter;

    public PropertiesEditorPresenterImpl(EmfDataset dataset, DatasetPropertiesEditorView view, EmfSession session) {
        this.dataset = dataset;
        this.session = session;
        this.view = view;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);

        dataset = dataService().obtainLockedDataset(session.user(), dataset);
        if (!dataset.isLocked(session.user())) {// view mode, locked by another user
            view.notifyLockFailure(dataset);
            return;
        }

        display();
    }

    void display() throws EmfException {
        Version[] versions = session.dataEditorService().getVersions(dataset.getId());
        view.display(dataset, versions);
    }

    public void doClose() throws EmfException {
        dataService().releaseLockedDataset(dataset);
        view.close();
    }

    public void doSave() throws EmfException {
        save(dataService(), summaryPresenter, keywordsPresenter, notesPresenter, qaPresenter);
    }

    void save(DataService service, EditableSummaryTabPresenter summary, EditableKeywordsTabPresenter keywords,
            EditNotesTabPresenter notes, EditableQAStepsPresenter qaPresenter) throws EmfException {
        updateDataset(service, summary, keywords, notes, qaPresenter);
        view.close();
    }

    private DataService dataService() {
        return session.dataService();
    }

    void updateDataset(DataService service, EditableSummaryTabPresenter summary, EditableKeywordsTabPresenter keywords,
            EditNotesTabPresenter notes, EditableQAStepsPresenter qaStep) throws EmfException {
        summary.doSave();
        keywords.doSave();
        notes.doSave();
        qaStep.doSave();
        service.updateDataset(dataset);
    }

    public void set(EditableSummaryTabView summary) {
        summaryPresenter = new EditableSummaryTabPresenterImpl(dataset, summary);
    }

    public void set(EditableKeywordsTabView keywordsView) throws EmfException {
        keywordsPresenter = new EditableKeywordsTabPresenterImpl(keywordsView, dataset);

        Keywords keywords = new Keywords(session.dataCommonsService().getKeywords());
        keywordsPresenter.display(keywords);
    }

    public void set(EditNotesTabView view) throws EmfException {
        notesPresenter = new EditNotesTabPresenterImpl(dataset, session, view);
        notesPresenter.display();
    }

    public void set(EditableQATabView qaTab) throws EmfException {
        qaPresenter = new EditableQAStepsPresenterImpl(dataset, session.qaService(), qaTab);
        set(qaPresenter);
    }

    void set(EditableQAStepsPresenter presenter) throws EmfException {
        presenter.display();
    }

}
