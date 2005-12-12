package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;

public interface VersionsView {

    void observe(VersionsPresenter presenter);

    void add(Version version);
}
