package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.InternalSource;

public interface VersionsView {

    void observe(VersionsPresenter presenter);

    void add(Version version);

    void reload(Version[] versions);

    void display(Version[] versions, InternalSource[] sources);
}
