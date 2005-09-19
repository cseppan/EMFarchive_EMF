package gov.epa.emissions.framework.client;

public class EmfConsolePresenter {

    private EmfConsoleView view;

    public void display(EmfConsoleView view) {
        this.view = view;
        view.observe(this);
        
        view.display();
    }

    public void notifyManageUsers() {
        view.displayUserManager();
    }

}
