package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.ChangeObserver;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.data.Keywords;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabPresenter;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabView;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabPresenter;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabView;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.EmfDataset;

public class PropertiesEditorPresenterImpl implements ChangeObserver, PropertiesEditorPresenter {

    private EmfDataset dataset;

    private PropertiesEditorView view;

    private EditableSummaryTabPresenter summaryPresenter;

    private boolean unsavedChanges;

    private EditableKeywordsTabPresenter keywordsPresenter;

    private ServiceLocator serviceLocator;

    private EmfSession session;

    public PropertiesEditorPresenterImpl(EmfDataset dataset, ServiceLocator serviceLocator, EmfSession session) {
        this.dataset = dataset;
        this.serviceLocator = serviceLocator;
        this.session = session;
    }

    public void doDisplay(PropertiesEditorView view) throws EmfException {
        this.view = view;
        view.observe(this);

        dataset = serviceLocator.dataService().obtainLockedDataset(session.user(), dataset);

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
            view.showError("Could not update dataset - " + dataset.getName() + ". Reason: " + e.getMessage());
            return;
        }

        clearChanges();
        view.close();
    }

    private DataService dataService() {
        return serviceLocator.dataService();
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

        Keywords keywords = new Keywords(serviceLocator.dataCommonsService().getKeywords());
        keywordsPresenter.display(keywords);
    }

    private void clearChanges() {
        unsavedChanges = false;
    }

    public void onChange() {
        unsavedChanges = true;
    }

}
