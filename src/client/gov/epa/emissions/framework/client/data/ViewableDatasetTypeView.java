package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.client.ManagedView;

public interface ViewableDatasetTypeView extends ManagedView {

    void observe(ViewableDatasetTypePresenter presenter);

    void display(DatasetType type);

    void close();

}
