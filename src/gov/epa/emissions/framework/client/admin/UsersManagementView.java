package gov.epa.emissions.framework.client.admin;

public interface UsersManagementView {
    void setViewObserver(UserManagerPresenter presenter);

    void close();

    void refresh();
}
