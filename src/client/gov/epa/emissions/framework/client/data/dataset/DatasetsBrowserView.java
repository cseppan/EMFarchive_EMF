package gov.epa.emissions.framework.client.data.dataset;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface DatasetsBrowserView extends ManagedView {
    
    void display(EmfDataset[] datasets) throws EmfException;

    void observe(DatasetsBrowserPresenter presenter);

    void refresh(EmfDataset[] datasets);

    void showMessage(String message);

    void showError(String message);

    void clearMessage();

    void notifyLockFailure(EmfDataset dataset);
    
    EmfDataset[] getSelected();

}
