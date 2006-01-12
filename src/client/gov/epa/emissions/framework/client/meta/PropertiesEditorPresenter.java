package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.ChangeObserver;
import gov.epa.emissions.framework.client.data.DatasetsBrowserView;
import gov.epa.emissions.framework.client.data.Keywords;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabPresenter;
import gov.epa.emissions.framework.client.meta.keywords.EditableKeywordsTabView;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabPresenter;
import gov.epa.emissions.framework.client.meta.summary.EditableSummaryTabView;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.DataService;
import gov.epa.emissions.framework.services.EmfDataset;

public class PropertiesEditorPresenter implements ChangeObserver {

    private EmfDataset dataset;

    private PropertiesEditorView view;

    private EditableSummaryTabPresenter summaryPresenter;

    private boolean unsavedChanges;

    private EditableKeywordsTabPresenter keywordsPresenter;

    private ServiceLocator serviceLocator;

    public PropertiesEditorPresenter(EmfDataset dataset, ServiceLocator serviceLocator) {
        this.dataset = dataset;
        this.serviceLocator = serviceLocator;
    }

    public void doDisplay(PropertiesEditorView view) {
        this.view = view;
        view.observe(this);

        view.display(dataset);
    }

    public void doClose() {
        if (unsavedChanges) {
            if (!view.shouldContinueLosingUnsavedChanges())
                return;
        }

        view.close();
    }

    public void doSave(DatasetsBrowserView browser) {
        DataService dataServices = serviceLocator.dataService();
        try {
            updateDataset(dataServices, summaryPresenter, keywordsPresenter);
        } catch (EmfException e) {
            view.showError("Could not update dataset - " + dataset.getName() + ". Reason: " + e.getMessage());
            return;
        }

        try {
            browser.refresh(dataServices.getDatasets());
        } catch (EmfException e) {
            browser.showError("Could not refresh Datasets, after updating " + dataset.getName());
            return;
        }

        clearChanges();
        doClose();
    }

    void updateDataset(DataService dataServices, EditableSummaryTabPresenter summary, EditableKeywordsTabPresenter keywords)
            throws EmfException {
        summary.doSave();
        keywords.doSave();
        dataServices.updateDatasetWithoutLock(dataset);
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
