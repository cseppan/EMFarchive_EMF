package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.InternalSource;

public interface InfoTabView {
    void displayInternalSources(InternalSource[] sources);

    void displayExternalSources(ExternalSource[] sources);
    
    void observe(InfoTabPresenter presenter);

}
