package gov.epa.emissions.framework.client.data.sector;

import gov.epa.emissions.framework.EmfException;

public interface EditableSectorPresenter {

    void doDisplay() throws EmfException;
    
    void doClose() throws EmfException;

    void doSave(SectorsManagerView sectorManager) throws EmfException;

}