/*
 * Created on Jun 21, 2005
 *
 */
package gov.epa.emissions.framework.service;

import gov.epa.emissions.framework.client.transport.EMFUserAdmin;
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
        
 //       UserAdminDummy uad = new UserAdminDummy();

        UserManagerDAO umDAO = new UserManagerDAO();
        
        User emfUser = umDAO.getUser(userName);
        if (emfUser == null){
            System.out.println("In the manager.  emfUser was NULL");
            status = "Incorrect User Name";    
        }//emfUser is null
        else{
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
        }//emfUser is not null

        System.out.println("end of authenticate with status= " + status);
       return status;        
 
        //return uad.authenticate(userName,pwd,wantAdminStatus);
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
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#createUser(gov.epa.emissions.framework.commons.User)
     */
    public String createUser(User newUser) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#updateUser(gov.epa.emissions.framework.commons.User)
     */
    public String updateUser(User newUser) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see gov.epa.emissions.framework.client.transport.EMFUserAdmin#deleteUser(java.lang.String)
     */
    public String deleteUser(String userName) {
        // TODO Auto-generated method stub
        return null;
    }
	
}
