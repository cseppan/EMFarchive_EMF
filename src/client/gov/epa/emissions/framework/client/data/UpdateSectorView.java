package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.client.EmfView;

public interface UpdateSectorView extends EmfView {
    void observe(UpdateSectorPresenter presenter);

    void display(Sector sector);
}
