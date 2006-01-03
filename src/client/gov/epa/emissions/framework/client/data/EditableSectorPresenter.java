package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.EmfException;

public interface EditableSectorPresenter {

    public abstract void doDisplay() throws EmfException;

    public abstract void doClose() throws EmfException;

    public abstract void doSave(SectorsManagerView sectorManager) throws EmfException;

}