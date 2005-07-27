/*
 * Created on Jun 21, 2005
 *
 */
package gov.epa.emissions.framework.service;

import gov.epa.emissions.framework.AuthenticationException;
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.InfrastructureException;
import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;
import gov.epa.emissions.framework.dao.UserManagerDAO;

import java.util.List;



/**
 * @author adm$wkstn
 *
 */
public class EMFUserManager implements EMFUserAdmin{

	/**
	 * 
	 */
	public EMFUserManager() {
		super();
		System.out.println("Created EMFUserManager");
	}
	
    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#isNewUser()
     */
    public boolean isNewUser() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#validate()
     */
    public boolean validate() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#authenticate(java.lang.String, java.lang.String, boolean)
     */
    public void authenticate(String userName, String pwd, boolean wantAdminStatus) throws EmfException {

        System.out.println("called authenticate for username= " + userName);

        String status = null;
        
        UserManagerDAO umDAO;
        try {
            umDAO = new UserManagerDAO();
            User emfUser = umDAO.getUser(userName);
            if (emfUser == null){
                System.out.println("In the manager.  emfUser was NULL");
                status = "Incorrect User Name";
                throw new AuthenticationException("Incorrect User Name");
            }//emfUser is null
            else{
             if (emfUser.isAcctDisabled()){
                 status="Account Disabled";
                 throw new AuthenticationException("Account Disabled");
             }else{
                 if (emfUser.getPassword().equals(pwd)){
                     if (!wantAdminStatus){
                         status="Valid";
                     }else{
                         if (emfUser.isInAdminGroup()){
                             status="Valid";
                         }else{
                             status="Cant Be Admin";
                             throw new AuthenticationException("Cant Be Admin");
                         }
                     }
                 }else{
                     status="Incorrect Password";
                     throw new AuthenticationException("Incorrect Password");
                 }
                 
             }
            }//emfUser is not null

        } catch (InfrastructureException ex) {
            ex.printStackTrace();
            throw new EmfException(ex.getMessage());
        } 

        System.out.println("end of authenticate with status= " + status);
 
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
            umDAO.insertUser(newUser);
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
