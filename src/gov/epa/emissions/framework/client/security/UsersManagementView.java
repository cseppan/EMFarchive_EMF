package gov.epa.emissions.framework.client.security;

public interface UsersManagementView {
    void setViewObserver(UsersManagementPresenter presenter);

    void close();

    void refresh();
}
