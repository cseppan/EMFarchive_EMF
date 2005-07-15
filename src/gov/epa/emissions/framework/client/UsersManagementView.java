package gov.epa.emissions.framework.client;

public interface UsersManagementView {
    void setViewObserver(UsersManagementPresenter presenter);

    void closeView();
}
