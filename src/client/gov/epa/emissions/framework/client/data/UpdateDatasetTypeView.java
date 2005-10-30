package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;

public interface UpdateDatasetTypeView {

    void observe(UpdateDatasetTypePresenter presenter);

    void display(DatasetType type);

    void close();

}
