package gov.epa.emissions.framework.client.admin;

public interface CreateUserView {
    String getUsername();

    String getPassword();

    String getConfirmPassword();
    
    String getName();

    String getEmail();

    String getPhone();

    String getAffiliation();
    
    void close();
    
    void setObserver(CreateUserPresenter presenter);
}
