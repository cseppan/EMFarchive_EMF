package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.EmfWidgetContainer;

public interface ImportView extends EmfWidgetContainer {
    void register(ImportPresenter presenter);
}
