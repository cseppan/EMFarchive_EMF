package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.EmfView;

public interface ImportView extends EmfView {
    void register(ImportPresenter presenter);

    void setDefaultBaseFolder(String folder);

    void clearMessagePanel();
}
