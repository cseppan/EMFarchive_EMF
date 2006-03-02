package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.framework.client.data.TableView;
import gov.epa.emissions.framework.client.data.viewer.TablePresenter;

public interface EditorPanelView extends TableView {
    void observe(TablePresenter presenter);

    ChangeSet changeset();

    void updateFilteredRecordsCount(int filtered);

}
