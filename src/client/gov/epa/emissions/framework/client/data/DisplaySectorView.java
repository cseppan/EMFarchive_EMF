package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.client.ManagedView;

public interface DisplaySectorView extends ManagedView {
    void observe(DisplaySectorPresenter presenter);

    void display(Sector sector);
}
