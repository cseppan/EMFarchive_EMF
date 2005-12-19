package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;

public interface PaginateOnlyTableView {
    void display(Page page);

    void observe(TablePresenter presenter);
}
