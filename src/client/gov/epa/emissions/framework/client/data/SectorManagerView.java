package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.client.ManagedView;

public interface SectorManagerView extends ManagedView {
    void observe(SectorManagerPresenter presenter);

    void display(Sector[] sectors);
}
