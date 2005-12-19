package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.ChangeSet;

public interface EditableTableView extends TableView {
    void observe(EditableTablePresenter presenter);

    ChangeSet changeset();
}
