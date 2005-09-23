package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.client.EmfView;
import gov.epa.emissions.framework.services.EmfDataset;

public interface DatasetsBrowserView extends EmfView {

    void observe(DatasetsBrowserPresenter presenter);

    void refresh(EmfDataset[] datasets);

    void showMessage(String message);

    void clearMessage();
}
