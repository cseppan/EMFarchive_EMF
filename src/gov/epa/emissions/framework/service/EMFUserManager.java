/*
 * Created on Jun 21, 2005
 *
 */
package gov.epa.emissions.framework.service;

import java.util.List;

import gov.epa.emissions.framework.commons.EMFUser;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;
import gov.epa.emissions.framework.dao.UserManagerDAO;



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
    public String authenticate(String userName, String pwd, boolean wantAdminStatus) {

        System.out.println("called authenticate for username= " + userName);

        String status = null;
        
        UserManagerDAO umDAO;
        try {
            umDAO = new UserManagerDAO();
            User emfUser = umDAO.getUser(userName);
            if (emfUser == null){
                System.out.println("In the manager.  emfUser was NULL");
                status = "Incorrect User Name";    
            }//emfUser is null
            else{
             if (emfUser.isAcctDisabled()){
                 status="Account Disabled";
             }else{
                 if (emfUser.getPassword().equals(pwd)){
                     if (!wantAdminStatus){
                         status="Valid";
                     }else{
                         if (emfUser.isInAdminGroup()){
                             status="Valid";
                         }else{
                             status="Cant Be Admin";
                         }
                     }
                 }else{
                     status="Incorrect Password";
                 }
                 
             }
            }//emfUser is not null

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("end of authenticate with status= " + status);
       return status;        
 
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
    public User getUser(String userName) {
        UserManagerDAO umDAO;
        User user = null;
        try {
            umDAO = new UserManagerDAO();
            user = umDAO.getUser(userName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return user;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#createUser(gov.epa.emissions.framework.commons.User)
     */
    public String createUser(User newUser) {
        String statuscode = "Success";
        UserManagerDAO umDAO;
        try {
            umDAO = new UserManagerDAO();
            umDAO.insertUser(newUser);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return statuscode;       
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#updateUser(gov.epa.emissions.framework.commons.User)
     */
    public String updateUser(User newUser) {
        String statuscode = "Success";
        UserManagerDAO umDAO;
        try {
            umDAO = new UserManagerDAO();
            umDAO.updateUser(newUser);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return statuscode;       
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#deleteUser(java.lang.String)
     */
    public String deleteUser(String userName) {
        String statuscode = "Success";
        UserManagerDAO umDAO;
        try {
            umDAO = new UserManagerDAO();
            umDAO.deleteUser(userName);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return statuscode;       
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#getUsers()
     */
    public User[] getUsers() {
        User[] users = null;
        
        UserManagerDAO umDAO;
        try {
            umDAO = new UserManagerDAO();
            List allUsers = umDAO.getUsers();  
            users = (User[]) allUsers.toArray(new User[allUsers.size()]); 	
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return users;        
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#updateUsers(java.util.Collection)
     */
    public String updateUsers(List users) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.commons.EMFUserAdmin#getEmfUsers()
     */
    public EMFUser[] getEmfUsers() {
        System.out.println("In getEmfUsers");
        EMFUser[] all = new EMFUser[2];
        
        EMFUser user = new EMFUser();
        user.setName("Fredreick");
        all[0] = user;
        EMFUser user2 = new EMFUser();
        user.setName("Joanne");
        all[1] = user2;
        System.out.println("End getEmfUsers: " + all.length);

        return all;
    }
	
}
