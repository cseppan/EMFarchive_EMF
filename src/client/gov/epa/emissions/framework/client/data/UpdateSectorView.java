package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.client.ManagedView;

public interface UpdateSectorView extends ManagedView {
    void observe(UpdateSectorPresenter presenter);

    void display(Sector sector);
}
