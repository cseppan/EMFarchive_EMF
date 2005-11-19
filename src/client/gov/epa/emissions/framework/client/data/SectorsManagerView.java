package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.DataService;

public interface SectorsManagerView extends ManagedView {
    void observe(SectorsManagerPresenter presenter);

    void display(DataService dataServices) throws EmfException;

    void refresh();
}
