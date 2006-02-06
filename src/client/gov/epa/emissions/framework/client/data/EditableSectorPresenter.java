package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.EmfException;

public interface EditableSectorPresenter {

    void doDisplay() throws EmfException;
    
    void displayNewSector();

    void doClose() throws EmfException;

    void doSave(SectorsManagerView sectorManager) throws EmfException;

}