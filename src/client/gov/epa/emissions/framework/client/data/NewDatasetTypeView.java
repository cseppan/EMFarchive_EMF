package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.client.ManagedView;

public interface NewDatasetTypeView extends ManagedView {

    void observe(NewDatasetTypePresenter presenter);

    void display();

    void close();

}