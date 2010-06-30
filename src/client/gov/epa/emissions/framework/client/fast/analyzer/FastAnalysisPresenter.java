package gov.epa.emissions.framework.client.fast.analyzer;

import gov.epa.emissions.framework.services.EmfException;

public interface FastAnalysisPresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave() throws EmfException;

    void addTab(FastAnalysisTabView view) throws EmfException;

    void fireTracking();

    boolean hasResults();
}
