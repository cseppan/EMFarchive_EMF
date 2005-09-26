package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.EmfView;

public interface ExportView extends EmfView {

    void observe(ExportPresenter presenter);

    void setMostRecentUsedFolder(String mostRecentUsedFolder);

}
