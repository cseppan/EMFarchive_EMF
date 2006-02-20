package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.io.DatasetType;

public class ViewableDatasetTypePresenterImpl implements ViewableDatasetTypePresenter {

    private ViewableDatasetTypeView view;

    private DatasetType type;

    public ViewableDatasetTypePresenterImpl(ViewableDatasetTypeView view, DatasetType type) {
        this.view = view;
        this.type = type;
    }

    public void doDisplay() {
        view.observe(this);
        view.display(type);
    }

    public void doClose() {
        view.close();
    }

}
