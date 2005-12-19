package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;

public interface EditsAwareTableView {
    void display(Page page);

    void observe(EditsAwareTablePresenter presenter);
}
