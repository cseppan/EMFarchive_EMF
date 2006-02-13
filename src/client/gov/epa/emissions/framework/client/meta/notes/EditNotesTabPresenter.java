package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.EmfException;

public interface EditNotesTabPresenter {

    public abstract void display() throws EmfException;

    public abstract void doSave() throws EmfException;

}