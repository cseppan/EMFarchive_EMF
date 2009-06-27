package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.client.meta.PropertiesViewPresenter;
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

    public void display(DatasetType defaultType, boolean selectSingle) throws Exception {
        view.observe(this);

        //get data...
        DatasetType[] datasetTypes = new DatasetType[] {};
        if (datasetTypesToInclude == null)
            datasetTypes = session.dataCommonsService().getDatasetTypes();
        else
            datasetTypes = datasetTypesToInclude;

        view.display(datasetTypes, defaultType, selectSingle);
    }
    
    public void refreshDatasets(DatasetType datasetType, String nameContaining) throws EmfException {
        view.refreshDatasets(session.dataService().getDatasets(datasetType.getId(), nameContaining));
    }
    
    public EmfDataset[] getDatasets() throws EmfException {
        //get full, because future methods/functions might expect the fully populated object.
        return getFullDatasets(view.getDatasets());
    }
    
    private EmfDataset[] getFullDatasets(EmfDataset[] lightDatasets) throws EmfException {
        EmfDataset[] datasets = new EmfDataset[lightDatasets.length];

        for(int i = 0; i < lightDatasets.length; ++i) {
            datasets[i] = session.dataService().getDataset(lightDatasets[i].getId());
        }
            
        return datasets;
    }
    
    public void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException {
        view.clearMessage();

        PropertiesViewPresenter presenter = new PropertiesViewPresenter(dataset, session);
        presenter.doDisplay(propertiesView);
    }
    
    public EmfSession getSession(){
        return session; 
    }

    public EmfDataset getDatasets(int id) throws EmfException{
        return session.dataService().getDataset(id);
    }
    
}