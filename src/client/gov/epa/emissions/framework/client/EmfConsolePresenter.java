package gov.epa.emissions.framework.client;

public class EmfConsolePresenter implements EmfPresenter {

    private EmfConsoleView view;

    public EmfConsolePresenter(EmfConsoleView view) {
        this.view = view;
    }

    public void observe() {
        view.setObserver(this);
    }

    public void notifyManageUsers() {
        view.displayUserManager();
    }

}
