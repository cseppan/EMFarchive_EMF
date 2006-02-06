package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.exim.ExportPresenter;
import gov.epa.emissions.framework.client.exim.ExportView;
import gov.epa.emissions.framework.client.exim.ImportPresenter;
import gov.epa.emissions.framework.client.exim.ImportView;
import gov.epa.emissions.framework.client.meta.PropertiesEditorPresenter;
import gov.epa.emissions.framework.client.meta.PropertiesEditorPresenterImpl;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesEditorView;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
import gov.epa.emissions.framework.client.meta.versions.VersionedDataPresenter;
import gov.epa.emissions.framework.client.meta.versions.VersionedDataView;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.EmfDataset;

public class DatasetsBrowserPresenter implements Browser {

    private DatasetsBrowserView view;

    private ServiceLocator serviceLocator;

    private EmfSession session;

    public DatasetsBrowserPresenter(EmfSession session, ServiceLocator serviceLocator) {
        this.session = session;
        this.serviceLocator = serviceLocator;
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
            view.showMessage("To Export, you will need to select at least one non-External type Dataset");
            return;
        }

        view.clearMessage();
        presenter.display(exportView);
    }

    public void doRefresh() throws EmfException {
        view.refresh(serviceLocator.dataService().getDatasets());
        view.clearMessage();
    }

    public void doDisplayPropertiesEditor(DatasetPropertiesEditorView propertiesEditorView, EmfDataset dataset)
            throws EmfException {
        PropertiesEditorPresenter presenter = new PropertiesEditorPresenterImpl(dataset, serviceLocator, session,this);
        doDisplayPropertiesEditor(propertiesEditorView, presenter);
    }

    void doDisplayPropertiesEditor(DatasetPropertiesEditorView propertiesEditorView, PropertiesEditorPresenter presenter)
            throws EmfException {
        view.clearMessage();
        presenter.doDisplay(propertiesEditorView);
    }

    public void doImport(ImportView importView, ImportPresenter importPresenter) {
        view.clearMessage();
        importPresenter.display(importView);
    }

    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) {
        view.clearMessage();

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset);
        presenter.doDisplay(propertiesView);
    }

    public void doDisplayVersionedData(VersionedDataView versionsView, EmfDataset dataset) {
        view.clearMessage();

        VersionedDataPresenter presenter = new VersionedDataPresenter(session.user(), dataset, serviceLocator
                .dataEditorService(), serviceLocator.dataViewService());
        presenter.display(versionsView);
    }

    public void notifyUpdates() {
        try {
            doRefresh();
        } catch (EmfException e) {
            view.showError(e.getMessage());
        }
        
    }

}
