package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class InputDatasetSelectionPresenter {

    private EmfSession session;

    private InputDatasetSelectionView view;
    
    private DatasetType[] datasetTypesToInclude;

    public InputDatasetSelectionPresenter(InputDatasetSelectionView view, EmfSession session,
            DatasetType[] datasetTypesToInclude) {
        this(view, session);
        this.datasetTypesToInclude = datasetTypesToInclude;
    }

    public InputDatasetSelectionPresenter(InputDatasetSelectionView view, EmfSession session) {
        this.session = session;
        this.view = view;
    }

    public void display(DatasetType defaultType) throws Exception {
        view.observe(this);

        //get data...
        DatasetType[] datasetTypes = new DatasetType[] {};
        if (datasetTypesToInclude == null)
            datasetTypes = session.dataCommonsService().getDatasetTypes();
        else
            datasetTypes = datasetTypesToInclude;

        view.display(datasetTypes, defaultType);
    }
    
    public void refreshDatasets(DatasetType datasetType, String nameContaining) throws EmfException {
        view.refreshDatasets(session.dataService().getDatasets(datasetType.getId(), nameContaining));
    }

    public EmfDataset[] getDatasets() {
        return view.getDatasets();
    }
}