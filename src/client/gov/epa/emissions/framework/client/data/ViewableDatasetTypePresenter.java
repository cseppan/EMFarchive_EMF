package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.services.DataCommonsService;

public class ViewableDatasetTypePresenter {

    private ViewableDatasetTypeView view;

    private DatasetType type;

    private DataCommonsService service;

    public ViewableDatasetTypePresenter(ViewableDatasetTypeView view, DatasetType type, DataCommonsService service) {
        this.view = view;
        this.type = type;
        this.service = service;
    }

    public void doDisplay() throws EmfException {
        view.observe(this);
        view.display(type, service.getKeywords());
    }

    public void doClose() {
        view.close();
    }

}
