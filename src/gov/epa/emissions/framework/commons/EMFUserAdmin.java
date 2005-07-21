/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFCommons
 * Package: package gov.epa.emissions.framework.client.transport;
 * File Name: EMFUserAdmin.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.commons;

import gov.epa.emissions.framework.EmfException;

import java.util.List;


/**
 * @author Conrad F. D'Cruz
 *
 * This is the interface to the EMF systems User Admin functions
 * The functions are implemented in the EMFUserAdminTransport class.
 * 
 */
public interface EMFUserAdmin {

    public boolean isNewUser() throws EmfException;
    public boolean validate() throws EmfException;
    public String authenticate(String userName, String pwd, boolean wantAdminStatus) throws EmfException;    
    public boolean resetPassword() throws EmfException;
    public User getUser(String userName) throws EmfException;
    public User[] getUsers() throws EmfException;
    public String createUser(User newUser) throws EmfException;
    public String updateUser(User newUser) throws EmfException;
    public String updateUsers(List users) throws EmfException;
    public String deleteUser(String userName) throws EmfException;
    public EMFUser[] getEmfUsers() throws EmfException;
}
