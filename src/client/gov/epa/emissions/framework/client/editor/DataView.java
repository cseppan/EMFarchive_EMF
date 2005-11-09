package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.io.Dataset;
import gov.epa.emissions.framework.client.ManagedView;

public interface DataView extends ManagedView {
    void display(Dataset dataset);
}
