package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.client.ManagedView;

public interface DatasetTypesManagerView extends ManagedView {
    void observe(DatasetTypesManagerPresenter presenter);

    void display(DatasetType[] types);

    void refresh();
}
