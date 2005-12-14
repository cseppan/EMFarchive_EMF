package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.InternalSource;

public interface DataTabView {

    void displayVersions(Version[] versions, InternalSource[] sources);
}
