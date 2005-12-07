package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;

public interface TableView {
    void observe(TableViewPresenter presenter);

    void display(Page page);

}
