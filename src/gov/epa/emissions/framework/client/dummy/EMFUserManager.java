/**
 * EMFUserManager.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package gov.epa.emissions.framework.client.dummy;

public interface EMFUserManager extends java.rmi.Remote {
    public boolean validate() throws java.rmi.RemoteException;
    public java.lang.String authenticate(java.lang.String userName, java.lang.String pwd, boolean wantAdminStatus) throws java.rmi.RemoteException;
    public java.lang.Object[] getUsers() throws java.rmi.RemoteException;
    public java.lang.String createUser(gov.epa.emissions.framework.services.User newUser) throws java.rmi.RemoteException;
    public boolean isNewUser() throws java.rmi.RemoteException;
    public gov.epa.emissions.framework.services.User getUser(java.lang.String userName) throws java.rmi.RemoteException;
    public boolean resetPassword() throws java.rmi.RemoteException;
    public java.lang.String updateUser(gov.epa.emissions.framework.services.User newUser) throws java.rmi.RemoteException;
    public java.lang.String deleteUser(java.lang.String userName) throws java.rmi.RemoteException;
    public java.lang.String updateUsers(java.lang.Object[] users) throws java.rmi.RemoteException;
}
