package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DatasetTypesServices;
import gov.epa.emissions.framework.services.InterDataServices;

public class UpdateDatasetTypePresenter {

    private UpdateDatasetTypeView view;

    private DatasetType type;

    private DatasetTypesServices datasetTypesServices;

    private InterDataServices interdataServices;

    public UpdateDatasetTypePresenter(UpdateDatasetTypeView view, DatasetType type,
            DatasetTypesServices datasetTypesServices, InterDataServices interdataServices) {
        this.view = view;
        this.type = type;
        this.datasetTypesServices = datasetTypesServices;
        this.interdataServices = interdataServices;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display(type, interdataServices.getKeywords());
    }

    public void doClose() {
        view.close();
    }

    public void doSave(DatasetTypesManagerView manager) throws EmfException {
        datasetTypesServices.updateDatasetType(type);
        manager.refresh();
        doClose();
    }

}
