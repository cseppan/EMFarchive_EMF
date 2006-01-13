package gov.epa.emissions.framework.client.meta;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        dataset = serviceLocator.dataService().obtainLockedDataset(session.user(), dataset);

        if (!dataset.isLocked(session.user())) {// view mode, locked by another user
            throw new EmfException("Dataset cannot be edited as it is locked by another user ("
                    + dataset.getLockOwner() + ") at " + format(dataset.getLockDate()));
        }

        this.view = view;
        view.observe(this);
        view.display(dataset);
    }

    private String format(Date lockDate) {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        return dateFormat.format(lockDate);
    }

    public void doClose() {
        if (unsavedChanges) {
            if (!view.shouldContinueLosingUnsavedChanges())
                return;
        }

        view.close();
    }

    public void doSave() {
        DataService service = serviceLocator.dataService();
        try {
            updateDataset(service, summaryPresenter, keywordsPresenter);
        } catch (EmfException e) {
            view.showError("Could not update dataset - " + dataset.getName() + ". Reason: " + e.getMessage());
            return;
        }

        clearChanges();
        doClose();
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
