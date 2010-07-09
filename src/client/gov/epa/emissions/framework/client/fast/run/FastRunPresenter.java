package gov.epa.emissions.framework.client.fast.run;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.fast.run.tabs.FastRunTabView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface FastRunPresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave() throws EmfException;

    void doRun() throws EmfException;

    void doRefresh() throws EmfException;

    void addTab(FastRunTabView view) throws EmfException;

    void fireTracking();

    boolean hasResults();

    DatasetType getDatasetType(String dataset) throws EmfException;

    Version[] getVersions(EmfDataset dataset) throws EmfException;
}
