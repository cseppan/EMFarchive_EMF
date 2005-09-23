package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.exim.ExportPresenter;
import gov.epa.emissions.framework.client.exim.ExportView;
import gov.epa.emissions.framework.client.meta.MetadataPresenter;
import gov.epa.emissions.framework.client.meta.MetadataView;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EmfDataset;
import gov.epa.emissions.framework.ui.WindowLayoutManager;

public class DatasetsBrowserPresenter {

    private DatasetsBrowserView view;

    private WindowLayoutManager windowLayoutManager;

    private DataServices dataServices;

    public DatasetsBrowserPresenter(DataServices dataServices, WindowLayoutManager windowLayoutManager) {
        this.dataServices = dataServices;
        this.windowLayoutManager = windowLayoutManager;
    }

    public void display(DatasetsBrowserView view) {
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
        presenter.display(exportView);
    }

    public void doRefresh() throws EmfException {
        // FIXME: fix the type casting
        view.refresh(dataServices.getDatasets());
        
        view.clearMessage();
    }

    public void doShowMetadata(MetadataView metadataView, EmfDataset dataset) {
        MetadataPresenter presenter = new MetadataPresenter(dataset, dataServices);
        windowLayoutManager.add(metadataView);
        presenter.display(metadataView);

        view.clearMessage();
    }

}
