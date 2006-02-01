package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.framework.EmfException;

public interface EditableTablePresenter extends TablePresenter {

    boolean hasChanges();

    void submitChanges() throws EmfException;
}