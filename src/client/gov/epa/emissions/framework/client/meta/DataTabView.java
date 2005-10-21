package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.io.ExternalSource;
import gov.epa.emissions.commons.io.InternalSource;

public interface DataTabView {
	void displayInternalSources(InternalSource[] sources);

	void displayExternalSources(ExternalSource[] sources);
}
