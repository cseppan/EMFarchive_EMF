package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

import java.util.ArrayList;
import java.util.List;

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

    public void display() throws Exception {
        view.observe(this);

        //get data...
        DatasetType[] datasetTypes = new DatasetType[] {};
        if (datasetTypesToInclude == null)
            datasetTypes = session.dataCommonsService().getDatasetTypes();
        else
            datasetTypes = datasetTypesToInclude;

        //FIXME:  For now show only ORL dataset types...
        List<DatasetType> datasetTypeList = new ArrayList<DatasetType>();
        for (int i = 0; i < datasetTypes.length; i++)
            if (datasetTypes[i].getImporterClassName().indexOf("ORL") >= 0)
                datasetTypeList.add(datasetTypes[i]);

        view.display(datasetTypeList.toArray(new DatasetType[0]));
    }
    
    public void refreshDatasets(DatasetType datasetType) throws EmfException {
        view.refreshDatasets(session.dataService().getDatasets(datasetType));
    }

    public EmfDataset[] getDatasets() {
        return view.getDatasets();
    }
}