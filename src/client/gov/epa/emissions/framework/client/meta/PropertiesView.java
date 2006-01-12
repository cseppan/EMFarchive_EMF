package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfDataset;

public interface PropertiesView extends ManagedView {

    void observe(PropertiesViewPresenter presenter);

    void display(EmfDataset dataset);

    void showError(String message);

}
