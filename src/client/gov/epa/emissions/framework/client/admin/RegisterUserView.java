package gov.epa.emissions.framework.client.admin;

public interface RegisterUserView {

    void close();

    void observe(RegisterUserPresenter presenter);

    void display();
}
