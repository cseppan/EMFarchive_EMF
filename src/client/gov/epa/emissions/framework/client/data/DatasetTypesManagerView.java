package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.DatasetType;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.EmfConsole;

public interface DatasetTypesManagerView extends ManagedView {
    void observe(DatasetTypesManagerPresenter presenter);

    void refresh(DatasetType[] types);

    EmfConsole getParentConsole();

    void display(DatasetType[] datasetTypes);
}
