package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.io.ExternalSource;
import gov.epa.emissions.commons.io.InternalSource;

public interface InfoTabView {
    void displayInternalSources(InternalSource[] sources);

    void displayExternalSources(ExternalSource[] sources);
}
