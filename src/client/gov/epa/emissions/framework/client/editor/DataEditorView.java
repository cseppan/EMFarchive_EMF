package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.DataEditorService;
import gov.epa.emissions.framework.services.DataAccessToken;

public interface DataEditorView extends ManagedView {
    void display(Version version, String table, DataEditorService service);

    void observe(DataEditorPresenter presenter);

    void notifyLockFailure(DataAccessToken token);

}
