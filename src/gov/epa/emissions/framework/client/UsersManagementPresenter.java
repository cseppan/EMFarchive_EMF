package gov.epa.emissions.framework.client;

public class UsersManagementPresenter {

    private UsersManagementView view;

    public UsersManagementPresenter(UsersManagementView view) {
        this.view = view;
    }

    public void notifyCloseView() {
        this.view.closeView();
    }

    public void init() {
        this.view.setViewObserver(this);
    }

}
