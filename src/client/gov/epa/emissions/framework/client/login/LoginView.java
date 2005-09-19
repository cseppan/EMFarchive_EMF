package gov.epa.emissions.framework.client.login;

public interface LoginView {
    void setObserver(LoginPresenter presenter);

    void close();

    void display();

}
