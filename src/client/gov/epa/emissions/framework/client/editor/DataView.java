package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.Page;

public interface DataView extends ManagedView {
    void display(Page page);
}
