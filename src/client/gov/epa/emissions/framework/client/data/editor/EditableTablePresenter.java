package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.data.viewer.TablePresenter;

public interface EditableTablePresenter extends TablePresenter {

    boolean hasChanges();

    void submitChanges() throws EmfException;
}