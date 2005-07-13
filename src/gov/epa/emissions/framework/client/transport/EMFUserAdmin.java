/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFCommons
 * Package: package gov.epa.emissions.framework.client.transport;
 * File Name: EMFUserAdmin.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.framework.commons.User;

/**
 * @author Conrad F. D'Cruz
 *
 * This is the interface to the EMF systems User Admin functions
 * The functions are implemented in the EMFUserAdminTransport class.
 * 
 */
public interface EMFUserAdmin {

    public boolean isNewUser();
    public boolean validate();
    public String authenticate(String userName, String pwd, boolean wantAdminStatus);    
    public boolean resetPassword();
    public User getUser(String userName);
    public String createUser(User newUser);
    public String updateUser(User newUser);    
    public String deleteUser(String userName);
}
