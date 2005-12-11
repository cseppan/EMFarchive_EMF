package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.ManagedView;

public interface VersionedDataView extends ManagedView {
    void display(Version version, String table);
}
