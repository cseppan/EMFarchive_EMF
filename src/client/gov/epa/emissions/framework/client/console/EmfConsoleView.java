package gov.epa.emissions.framework.client.console;


public interface EmfConsoleView {
    void displayUserManager();

    void observe(EmfConsolePresenter presenter);

    void display();
    
    int height();
    
    int width();

}
