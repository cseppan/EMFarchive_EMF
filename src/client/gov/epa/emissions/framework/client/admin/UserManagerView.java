package gov.epa.emissions.framework.client.admin;

public interface UserManagerView {

    void setObserver(UserManagerPresenter presenter);

    void close();

    void refresh();
}
