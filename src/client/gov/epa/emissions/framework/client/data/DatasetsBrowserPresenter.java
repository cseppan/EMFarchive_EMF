package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.exim.ExportPresenter;
import gov.epa.emissions.framework.client.meta.MetadataPresenter;
import gov.epa.emissions.framework.client.meta.MetadataView;
import gov.epa.emissions.framework.services.DataServices;
import gov.epa.emissions.framework.services.EmfDataset;

public class DatasetsBrowserPresenter {

    private DatasetsBrowserView view;

    private EmfSession session;

    public DatasetsBrowserPresenter(EmfSession session) {
        this.session = session;
    }

    public void display(DatasetsBrowserView view) {
        this.view = view;
        view.observe(this);

        view.display();
    }

    public void doClose() {
        view.close();
    }

    public void doExport(EmfDataset[] datasets) throws EmfException {
        if (datasets.length == 0) {
            view.showMessage("To Export, you will need to select at least one Dataset");
            return;
        }
        
        ExportPresenter presenter = new ExportPresenter(session);
        view.showExport(datasets, presenter);
        
        view.clearMessage();
    }

    public void doRefresh() throws EmfException {
        DataServices dataServices = session.getDataServices();
        // FIXME: fix the type casting
        view.refresh((EmfDataset[]) dataServices.getDatasets());
        
        view.clearMessage();
    }

    // FIXME: change other presenters to follow this design
    public void notifyShowMetadata(MetadataView metadataView, EmfDataset dataset) {
        MetadataPresenter presenter = new MetadataPresenter(dataset);
        presenter.display(metadataView);
        
        view.clearMessage();
    }

}
