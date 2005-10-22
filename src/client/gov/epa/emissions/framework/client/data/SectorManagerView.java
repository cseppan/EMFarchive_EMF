package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.Sector;

public interface SectorManagerView extends ManagedView {
    void observe(SectorManagerPresenter presenter);

    void display(Sector[] sectors);
}
