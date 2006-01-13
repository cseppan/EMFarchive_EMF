package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.exim.ExportPresenter;
import gov.epa.emissions.framework.client.exim.ExportView;
import gov.epa.emissions.framework.client.exim.ImportPresenter;
import gov.epa.emissions.framework.client.exim.ImportView;
import gov.epa.emissions.framework.client.meta.PropertiesEditorPresenter;
import gov.epa.emissions.framework.client.meta.PropertiesEditorView;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.client.meta.versions.VersionedDataPresenter;
import gov.epa.emissions.framework.client.meta.versions.VersionedDataView;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.ui.ViewLayout;

public class DatasetsBrowserPresenter {

    private DatasetsBrowserView view;

    private ViewLayout viewLayout;

    private ServiceLocator serviceLocator;

    public DatasetsBrowserPresenter(ServiceLocator serviceLocator, ViewLayout viewLayout) {
        this.serviceLocator = serviceLocator;
        this.viewLayout = viewLayout;
    }

    public void doDisplay(DatasetsBrowserView view) {
        this.view = view;
        view.observe(this);

        view.display();
    }

    public void doClose() {
        view.close();
    }

    public void doExport(ExportView exportView, ExportPresenter presenter, EmfDataset[] datasets) {
        if (datasets.length == 0) {
            view.showMessage("To Export, you will need to select at least one Dataset");
            return;
        }

        view.clearMessage();
        // TODO: attache the dataset names to the id
        viewLayout.add(exportView, "Export Datasets");

        presenter.display(exportView);
    }

    public void doRefresh() throws EmfException {
        view.refresh(serviceLocator.dataService().getDatasets());

        view.clearMessage();
    }

    public void doDisplayPropertiesEditor(PropertiesEditorView propertiesEditorView, EmfDataset dataset) {
        view.clearMessage();
        if (viewLayout.activate("Properties Editor - " + dataset.getName()))
            return;

        viewLayout.add(propertiesEditorView, "Properties Editor - " + dataset.getName());

        PropertiesEditorPresenter presenter = new PropertiesEditorPresenter(dataset, serviceLocator);
        presenter.doDisplay(propertiesEditorView);
    }

    // TODO: Is doDisplayPropertiesEditor a better style/pattern compared to doImport ?
    public void doImport(ImportView importView, ImportPresenter importPresenter) throws EmfException {
        view.clearMessage();
        viewLayout.add(importView, "Datasets Browser - Import");

        importPresenter.display(importView);
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) {
        view.clearMessage();
        if (viewLayout.activate("Properties View - " + dataset.getName()))
            return;
        viewLayout.add(propertiesView, "Properties View - " + dataset.getName());

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset);
        presenter.doDisplay(propertiesView);
    }

    public void doDisplayVersionsEditor(VersionedDataView versionsView, EmfDataset dataset) {
        view.clearMessage();
        if (viewLayout.activate("Versions Editor - " + dataset.getName()))
            return;
        viewLayout.add(versionsView, "Versions Editor - " + dataset.getName());

        VersionedDataPresenter presenter = new VersionedDataPresenter(dataset, serviceLocator.dataEditorService());
        presenter.display(versionsView);
    }

}
