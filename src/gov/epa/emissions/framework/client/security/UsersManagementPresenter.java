package gov.epa.emissions.framework.client.security;

public class UsersManagementPresenter {

    private UsersManagementView view;

    public UsersManagementPresenter(UsersManagementView view) {
        this.view = view;
    }

    public void notifyCloseView() {
        this.view.close();
    }

    public void init() {
        this.view.setViewObserver(this);
    }

}
