package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.DataAccessService;

public interface DataView extends ManagedView {
    void display(Version version, String table, DataAccessService service);

    void observe(DataViewPresenter presenter);
}
