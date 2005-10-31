package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.DataServices;

public interface SectorManagerView extends ManagedView {
    void observe(SectorsManagerPresenter presenter);

    void display(DataServices dataServices) throws EmfException;

    void refresh();
}
