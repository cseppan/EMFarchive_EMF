package gov.epa.emissions.framework.client.admin;

public interface RegisterUserView {
    String getUsername();

    String getPassword();

    String getConfirmPassword();
    
    String getFullName();

    String getEmail();

    String getPhone();

    String getAffiliation();
    
    void close();
    
    void setObserver(RegisterUserPresenter presenter);
}
