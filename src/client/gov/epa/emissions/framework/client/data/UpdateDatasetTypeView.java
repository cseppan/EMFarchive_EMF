package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.client.ManagedView;

public interface UpdateDatasetTypeView extends ManagedView {

    void observe(UpdateDatasetTypePresenter presenter);

    void display(DatasetType type);

    void close();

}
