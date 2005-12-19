package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;

public interface NonEditableTableView {
    void display(Page page);

    void observe(TablePresenter presenter);
}
