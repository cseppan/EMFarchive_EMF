package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.framework.client.data.TableView;

public interface ViewerPanelView extends TableView {

    void observe(TablePresenter presenter);

    void updateFilteredRecordsCount(int filtered);
}
