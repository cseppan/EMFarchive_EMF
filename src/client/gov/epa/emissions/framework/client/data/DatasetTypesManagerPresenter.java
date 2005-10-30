package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DatasetTypesServices;

public class DatasetTypesManagerPresenter {

    private DatasetTypesManagerView view;

    private DatasetTypesServices services;

    public DatasetTypesManagerPresenter(DatasetTypesManagerView view, DatasetTypesServices services) {
        this.view = view;
        this.services = services;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display(services);
    }

    public void doClose() {
        view.close();
    }

    public void doUpdate(DatasetType type, UpdateDatasetTypeView updateView) {
        UpdateDatasetTypePresenter p = new UpdateDatasetTypePresenter(updateView, type, services);
        p.doDisplay();
    }
}
