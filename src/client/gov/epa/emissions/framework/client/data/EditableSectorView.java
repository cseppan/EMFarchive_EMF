package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.io.Sector;
import gov.epa.emissions.framework.client.ManagedView;

public interface EditableSectorView extends ManagedView {
    void observe(EditableSectorPresenter presenter);

    void display(Sector sector);
}
