package gov.epa.emissions.framework.client.login;

public interface LoginView {
    void observe(LoginPresenter presenter);

    void close();

    void display();

}
