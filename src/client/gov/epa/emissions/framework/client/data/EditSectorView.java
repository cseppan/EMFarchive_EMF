package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.client.ManagedView;

public interface EditSectorView extends ManagedView {
    void observe(EditSectorPresenter presenter);

    void display(Sector sector);
}
