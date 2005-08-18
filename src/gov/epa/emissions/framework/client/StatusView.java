package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.services.Status;

public interface StatusView extends EmfWidgetContainer {

    void update(Status[] messages);

    void notifyError(String message);

}
