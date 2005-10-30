package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.DatasetTypesServices;

public interface DatasetTypesManagerView extends ManagedView {
    void observe(DatasetTypesManagerPresenter presenter);

    void display(DatasetTypesServices services) throws EmfException;

    void refresh();
}
