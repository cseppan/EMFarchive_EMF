package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.services.EmfException;

public interface EmfConsoleView {
    void displayUserManager() throws EmfException;

    void observe(EmfConsolePresenter presenter);

    void display();

    int height();

    int width();

    boolean confirm();

}
