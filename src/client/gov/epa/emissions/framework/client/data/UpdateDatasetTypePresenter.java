package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DatasetTypesServices;

public class UpdateDatasetTypePresenter {

    private UpdateDatasetTypeView view;

    private DatasetType type;

    private DatasetTypesServices services;

    public UpdateDatasetTypePresenter(UpdateDatasetTypeView view, DatasetType type, DatasetTypesServices services) {
        this.view = view;
        this.type = type;
        this.services = services;
    }

    public void doDisplay() {
        view.observe(this);
        view.display(type);
    }

    public void doClose() {
        view.close();
    }

    public void doSave(DatasetTypesManagerView manager) throws EmfException {
        services.updateDatasetType(type);
        manager.refresh();
        doClose();
    }

}
