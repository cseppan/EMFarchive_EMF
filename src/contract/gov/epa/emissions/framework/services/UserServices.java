/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFCommons
 * Package: package gov.epa.emissions.framework.client.transport;
 * File Name: EMFUserAdmin.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import gov.epa.emissions.framework.EmfException;

/**
 * @author Conrad F. D'Cruz
 * Provides services for Login and Administrative functions
 */
public interface UserServices extends EMFServices{

    public void authenticate(String userName, String pwd, boolean wantAdminStatus) throws EmfException;

    public User getUser(String userName) throws EmfException;

    public User[] getUsers() throws EmfException;

    public void createUser(User newUser) throws EmfException;

    public void updateUser(User newUser) throws EmfException;

    public void updateUsers(User[] users) throws EmfException;

    //FIXME: should use a User object instead
    public void deleteUser(String userName) throws EmfException;

    // UNUSED methods
    public boolean resetPassword() throws EmfException;

}
