package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfDataset;

public interface DatasetsBrowserView extends ManagedView {

    void observe(DatasetsBrowserPresenter presenter);

    void refresh(EmfDataset[] datasets);

    void showMessage(String message);

    void showError(String message);

    void clearMessage();

}
