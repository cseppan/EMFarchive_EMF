package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.EmfException;

public interface RefreshObserver {

    void doRefresh() throws EmfException;

}
