package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.ui.ViewLayout;

public class DatasetTypesManagerPresenter {

    private DatasetTypesManagerView view;

    private ServiceLocator serviceLocator;

    private ViewLayout viewLayout;

    public DatasetTypesManagerPresenter(DatasetTypesManagerView view, ServiceLocator serviceLocator, ViewLayout layout) {
        this.view = view;
        this.serviceLocator = serviceLocator;
        this.viewLayout = layout;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display(serviceLocator.datasetTypeService());
    }

    public void doClose() {
        view.close();
    }

    public void doUpdate(DatasetType type, EditDatasetTypeView updateView) throws EmfException {
        if (viewLayout.activate(type.getName()))
            return;

        viewLayout.add(updateView, type.getName());
        EditDatasetTypePresenter p = new EditDatasetTypePresenter(updateView, type, serviceLocator
                .datasetTypeService(), serviceLocator.dataCommonsService());
        p.doDisplay();
    }
}
