package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.exim.ExportPresenter;
import gov.epa.emissions.framework.client.exim.ExportView;
import gov.epa.emissions.framework.client.exim.ImportPresenter;
import gov.epa.emissions.framework.client.exim.ImportView;
import gov.epa.emissions.framework.client.meta.PropertiesEditorPresenter;
import gov.epa.emissions.framework.client.meta.PropertiesEditorView;
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

    // FIXME: change other presenters to follow this design
    // Also, look at doShowMetadata to identify a better pattern
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
        view.refresh(serviceLocator.getDataServices().getDatasets());

        view.clearMessage();
    }

    // TODO: Is this a better style/pattern compared to doNew ?
    public void doShowProperties(PropertiesEditorView propertiesEditorView, EmfDataset dataset) {
        view.clearMessage();
        if (viewLayout.activate("Properties - " + dataset.getName()))
            return;

        showPropertiesEditor(propertiesEditorView, dataset);
    }

    private void showPropertiesEditor(PropertiesEditorView propertiesEditorView, EmfDataset dataset) {
        viewLayout.add(propertiesEditorView, "Properties - " + dataset.getName());

        PropertiesEditorPresenter presenter = new PropertiesEditorPresenter(dataset, serviceLocator);
        presenter.doDisplay(propertiesEditorView);
    }

    public void doNew(ImportView importView, ImportPresenter importPresenter) throws EmfException {
        view.clearMessage();
        viewLayout.add(importView, "Datasets Browser - Import");

        importPresenter.display(importView);
    }

}
