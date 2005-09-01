package gov.epa.emissions.framework.client.admin;

public interface UsersManagementView {

    void setObserver(UserManagerPresenter presenter);

    void close();

    void refresh();
}
