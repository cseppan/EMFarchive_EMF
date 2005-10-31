package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DatasetTypesServices;
import gov.epa.emissions.framework.ui.ViewLayout;

public class DatasetTypesManagerPresenter {

    private DatasetTypesManagerView view;

    private DatasetTypesServices services;

    private ViewLayout viewLayout;

    public DatasetTypesManagerPresenter(DatasetTypesManagerView view, DatasetTypesServices services, ViewLayout layout) {
        this.view = view;
        this.services = services;
        this.viewLayout = layout;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display(services);
    }

    public void doClose() {
        view.close();
    }

    public void doUpdate(DatasetType type, UpdateDatasetTypeView updateView) {
        if (viewLayout.activate(type.getName()))
            return;

        viewLayout.add(updateView, type.getName());
        UpdateDatasetTypePresenter p = new UpdateDatasetTypePresenter(updateView, type, services);
        p.doDisplay();
    }
}
