package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.EditToken;

public interface EditableDataView extends ManagedView {
    void display(Version version, String table, DataEditorService service);

    void observe(EditableDataViewPresenter presenter);

    void notifyLockFailure(EditToken token);

}
