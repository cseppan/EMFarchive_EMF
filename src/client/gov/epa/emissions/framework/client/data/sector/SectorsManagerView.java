package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.DataCommonsService;

public interface SectorsManagerView extends ManagedView {
    void observe(SectorsManagerPresenter presenter);

    void display(DataCommonsService service) throws EmfException;

    void refresh();
    
    EmfConsole getParentConsole();
}
