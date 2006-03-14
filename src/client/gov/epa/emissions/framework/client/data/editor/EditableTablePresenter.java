package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.framework.client.data.viewer.TablePresenter;
import gov.epa.emissions.framework.services.EmfException;

public interface EditableTablePresenter extends TablePresenter {

    boolean hasChanges();

    void submitChanges() throws EmfException;

    void reloadCurrent() throws EmfException;

}