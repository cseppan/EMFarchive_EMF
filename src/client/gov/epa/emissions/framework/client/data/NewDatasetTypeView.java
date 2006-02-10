package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.client.console.EmfConsole;

public interface NewDatasetTypeView extends ManagedView {

    void observe(NewDatasetTypePresenter presenter, EmfConsole parentConsole);

    void display();

    void close();

}