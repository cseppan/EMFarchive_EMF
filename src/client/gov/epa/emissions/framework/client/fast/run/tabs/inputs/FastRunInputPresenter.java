package gov.epa.emissions.framework.client.fast.run.tabs.inputs;

import gov.epa.emissions.framework.services.EmfException;

public interface FastRunInputPresenter {

    void doDisplay() throws EmfException;

    void doClose() throws EmfException;

    void doSave() throws EmfException;

    void fireTracking();

    boolean hasResults();
}
