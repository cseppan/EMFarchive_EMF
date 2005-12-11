package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;

public interface VersionedTableView {
    void display(Page page);

    void observe(VersionedTablePresenter presenter);
}
