package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.ChangeObserver;
import gov.epa.emissions.framework.client.data.DatasetsBrowserView;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EmfDataset;

public class PropertiesEditorPresenter implements ChangeObserver {

    private EmfDataset dataset;

    private PropertiesEditorView view;

    private DataServices dataServices;

    private SummaryTabPresenter summaryTabPresenter;

    private boolean unsavedChanges;

    public PropertiesEditorPresenter(EmfDataset dataset, DataServices dataServices) {
        this.dataset = dataset;
        this.dataServices = dataServices;
    }

    public void display(PropertiesEditorView view) {
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
        try {
            updateDataset();
        } catch (EmfException e) {
            view.showError("Could not update dataset - " + dataset.getName());
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

    void updateDataset() throws EmfException {
        summaryTabPresenter.doSave();
        dataServices.updateDataset(dataset);
    }

    public void add(SummaryTabView view) {
        summaryTabPresenter = new SummaryTabPresenter(dataset, view);
        view.observeChanges(this);
    }

    private void clearChanges() {
        unsavedChanges = false;
    }

    public void onChange() {
        unsavedChanges = true;
    }

}
