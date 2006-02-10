package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.DataCommonsService;

public interface DatasetTypesManagerView extends ManagedView {
    void observe(DatasetTypesManagerPresenter presenter);

    void display(DataCommonsService service) throws EmfException;

    void refresh();
    
    EmfConsole getParentConsole();
}
