package gov.epa.emissions.framework.client.editor;

import gov.epa.emissions.commons.db.version.ChangeSet;

public interface EditablePageManagerView extends TableView {
    void observe(EditableTablePresenterImpl presenter);

    ChangeSet changeset();

    void updateTotalRecordsCount(int total);

}
