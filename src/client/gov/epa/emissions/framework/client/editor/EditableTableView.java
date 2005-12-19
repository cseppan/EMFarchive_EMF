package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.Page;

public interface EditableTableView {
    void display(Page page);

    void observe(EditableTablePresenter presenter);
}
