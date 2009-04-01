package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;

public interface EditVersionsView {

    void observe(EditVersionsPresenter presenter);

    void add(Version version);

    void reload(Version[] versions);
    
    void refresh(); 

    void display(Version[] versions, InternalSource[] sources);

    void notifyLockFailure(Version version);

}
