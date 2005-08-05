/*
 * Created on Jun 21, 2005
 *
 */
package gov.epa.emissions.framework.service;

import gov.epa.emissions.framework.AuthenticationException;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.InfrastructureException;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.commons.UserServices;
import gov.epa.emissions.framework.commons.User;
import gov.epa.emissions.framework.dao.UserManagerDAO;

import java.util.List;



/**
 * @author adm$wkstn
 *
 */
public class UserServicesImpl implements UserServices{

	/**
	 * 
	 */
	public UserServicesImpl() {
		super();
		System.out.println("Created EMFUserManager");
	}

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#authenticate(java.lang.String, java.lang.String, boolean)
     */
    public void authenticate(String userName, String pwd, boolean wantAdminStatus) throws EmfException {

        System.out.println("called authenticate for username= " + userName);
       
        UserManagerDAO umDAO;
        try {
            umDAO = new UserManagerDAO();
            User emfUser = umDAO.getUser(userName);
            if (emfUser == null){
                System.out.println("In the manager.  emfUser was NULL");
                throw new AuthenticationException("Invalid username");
            }//emfUser is null
            else{
             if (emfUser.isAcctDisabled()){
                 throw new AuthenticationException("Account Disabled");
             }else{
                 if (!emfUser.getPassword().equals(pwd)){
                   throw new AuthenticationException("Incorrect Password");
                 }else{
                     if (wantAdminStatus){
                         if (!emfUser.isInAdminGroup()){
                             throw new AuthenticationException("Not authorized to log in as Administrator");
                         }//user not in Admin group   
                     }//want admin status
                 }//pwd correct                
             }//Acct not disabled
            }//emfUser is not null
        } catch (InfrastructureException ex) {
            ex.printStackTrace();
            throw new EmfException(ex.getMessage());
        } 

        System.out.println("end of authenticate");
 
    }//authenticate

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#resetPassword()
     */
    public boolean resetPassword() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#getUser(java.lang.String)
     */
    public User getUser(String userName) throws EmfException {
        UserManagerDAO umDAO;
        User user = null;
        try {
            umDAO = new UserManagerDAO();
            user = umDAO.getUser(userName);
        } catch (InfrastructureException ex) {
            ex.printStackTrace();
            throw new EmfException(ex.getMessage());
        } 
        return user;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#createUser(gov.epa.emissions.framework.commons.User)
     */
    public void createUser(User newUser) throws EmfException {
        
        UserManagerDAO umDAO;
        try {
            umDAO = new UserManagerDAO();
            if (umDAO.isNewUser(newUser.getUserName())){
                umDAO.insertUser(newUser);                
            }else{
                throw new UserException("Duplicate username");
            }
        } catch (InfrastructureException ex) {
            ex.printStackTrace();
            throw new EmfException(ex.getMessage());
        } 
        
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#updateUser(gov.epa.emissions.framework.commons.User)
     */
    public void updateUser(User newUser) throws EmfException {
        
        UserManagerDAO umDAO;
        try {
            umDAO = new UserManagerDAO();
            umDAO.updateUser(newUser);
        } catch (InfrastructureException ex) {
            ex.printStackTrace();
            throw new EmfException(ex.getMessage());
        }
               
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#deleteUser(java.lang.String)
     */
    public void deleteUser(String userName) throws EmfException {
        
        UserManagerDAO umDAO;
        try {
            umDAO = new UserManagerDAO();
            umDAO.deleteUser(userName);
        } catch (InfrastructureException ex) {
	        ex.printStackTrace();
	        throw new EmfException(ex.getMessage());
        }
          
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#getUsers()
     */
    
    public User[] getUsers() throws EmfException {
        User[] users = null;
        
        UserManagerDAO umDAO;
        try {
            umDAO = new UserManagerDAO();
            List allUsers = umDAO.getUsers();  
            users = (User[]) allUsers.toArray(new User[allUsers.size()]); 	
        } catch (InfrastructureException ex) {
	        ex.printStackTrace();
	        throw new EmfException(ex.getMessage());
        }
        return users;        
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.EMFUserAdmin#updateUsers(gov.epa.emissions.framework.commons.User[])
     */
    public void updateUsers(User[] users) throws EmfException {
        System.out.println("Start update Users");
        UserManagerDAO umDAO;
        try {
            umDAO = new UserManagerDAO();
            umDAO.updateUsers(users);               	
        } catch (InfrastructureException ex) {
	        ex.printStackTrace();
	        throw new EmfException(ex.getMessage());
        }
        System.out.println("End Update Users");
       
    }
    


}
