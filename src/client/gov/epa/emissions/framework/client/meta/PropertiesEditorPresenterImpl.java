package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabPresenterImpl;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabView;
import gov.epa.emissions.framework.client.meta.keywords.Keywords;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTabPresenterImpl;
import gov.epa.emissions.framework.client.meta.notes.EditNotesTabView;
import gov.epa.emissions.framework.client.meta.qa.EditableQAStepsPresenter;
import gov.epa.emissions.framework.client.meta.qa.EditableQAStepsPresenterImpl;
import gov.epa.emissions.framework.client.meta.qa.EditableQATabView;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabPresenterImpl;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataService;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PropertiesEditorPresenterImpl implements PropertiesEditorPresenter {

    private EmfDataset dataset;

    private DatasetPropertiesEditorView view;

    private EmfSession session;

    private List presenters;

    public PropertiesEditorPresenterImpl(EmfDataset dataset, DatasetPropertiesEditorView view, EmfSession session) {
        this.dataset = dataset;
        this.session = session;
        this.view = view;
        presenters = new ArrayList();
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
        save(dataset, dataService(), presenters, view);
    }

    void save(EmfDataset dataset, DataService service, List presenters, DatasetPropertiesEditorView view)
            throws EmfException {
        for (Iterator iter = presenters.iterator(); iter.hasNext();) {
            PropertiesEditorTabPresenter element = (PropertiesEditorTabPresenter) iter.next();
            element.doSave();
        }
        service.updateDataset(dataset);

        view.close();
    }

    public void set(EditableSummaryTabView summary) {
        EditableSummaryTabPresenterImpl summaryPresenter = new EditableSummaryTabPresenterImpl(dataset, summary);
        presenters.add(summaryPresenter);
    }

    public void set(EditableKeywordsTabView keywordsView) throws EmfException {
        EditableKeywordsTabPresenterImpl keywordsPresenter = new EditableKeywordsTabPresenterImpl(keywordsView, dataset);

        Keywords keywords = new Keywords(session.dataCommonsService().getKeywords());
        keywordsPresenter.display(keywords);

        presenters.add(keywordsPresenter);
    }

    public void set(EditNotesTabView view) throws EmfException {
        EditNotesTabPresenterImpl notesPresenter = new EditNotesTabPresenterImpl(dataset, session, view);
        notesPresenter.display();

        presenters.add(notesPresenter);
    }

    public void set(EditableQATabView qaTab) throws EmfException {
        EditableQAStepsPresenterImpl qaPresenter = new EditableQAStepsPresenterImpl(dataset, session, qaTab);
        set(qaPresenter);

        presenters.add(qaPresenter);
    }

    void set(EditableQAStepsPresenter presenter) throws EmfException {
        presenter.display();
    }

    private DataService dataService() {
        return session.dataService();
    }
}
