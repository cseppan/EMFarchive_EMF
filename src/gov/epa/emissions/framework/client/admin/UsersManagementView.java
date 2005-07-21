package gov.epa.emissions.framework.client.admin;

public interface UsersManagementView {
    void setViewObserver(UsersManagementPresenter presenter);

    void close();

    void refresh();
}
