package gov.epa.emissions.framework.client.admin;

public interface UserManagerView {

    void observe(UserManagerPresenter presenter);

    void close();

    void refresh();

    void display();
}
