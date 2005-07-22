package gov.epa.emissions.framework.client.admin;

public interface UserCreationView {
    String getUsername();

    String getPassword();

    String getConfirmPassword();
    
    String getName();

    String getEmail();

    String getPhone();

    String getAffiliation();
    
    void close();
    
}
