/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFClient
 * Package: package gov.epa.emissions.framework.service.axis;
 * File Name: EMFClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.client.dummy;
  
import gov.epa.emissions.framework.client.transport.EMFUserAdminTransport;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class EMFClient {

    /**
     * 
     */
    public EMFClient() {
        super();
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) {
        String statuscode = null;
        
        EMFUserAdmin emfUserAdmin = new EMFUserAdminTransport();
        System.out.println("IN EMFCLIENT main");
       
        String uname = "ejones";
        String pwd = "erin12345";
        
        statuscode = emfUserAdmin.authenticate(uname, pwd, true);        
        System.out.println(uname + " login status is: " + statuscode);
        statuscode = emfUserAdmin.authenticate(uname, pwd, false);        
        System.out.println(uname + " login status is: " + statuscode);
        User user = emfUserAdmin.getUser(uname);
        if (user == null){
            System.out.println("User object is null");
        }else{
            System.out.println("getUser succeeded: " + user.getFullName());
            
        }
/*        
        String status = null;
        uname = "fflintstone";
        pwd = "freddie";
        if (user != null){
            user.setUserName(uname);
            status = emfUserAdmin.createUser(user);            
        }else{
            System.out.println("encountered null user object");
        }
        System.out.println("status of create= " + status);
*/
//        user = emfUserAdmin.getUser(uname);
//        System.out.println("current password= " + user.getPassword());
//        System.out.println("setting password to " + pwd);
//        user.setPassword(pwd);
//       emfUserAdmin.updateUser(user);
//        user = emfUserAdmin.getUser(uname);
//        System.out.println("new password= " + user.getPassword());
//        String status = emfUserAdmin.deleteUser("jbond2");
//        System.out.println("Delete status= " + status);
//        user = emfUserAdmin.getUser(uname);
//        if (user != null){
//          System.out.println("username= " + user.getUserName());
//        }else{
//          System.out.println("No user found");
//        }    
//        EMFUser[] eusers = emfUserAdmin.getEmfUsers();
//        if (eusers == null){
//            System.out.println("Call returned a null USERS list");
//        }else{

//            System.out.println(eusers.length);
//        }
        
      User[] users = emfUserAdmin.getUsers();
      if (users == null){
          System.out.println("Call returned a null USERS list");
      }else{

          System.out.println(users.length);
      }
        
/*
        User[] users = emfUserAdmin.getUsers();
        if (users == null){
            System.out.println("Call returned a null USERS list");
        }else{

            Iterator iter = users.iterator();
            
            while (iter.hasNext()){
                User aUser = (User) iter.next();
                if (aUser !=null){
                    System.out.println(aUser.getFullName());                    
                }else{
                    System.out.println("User was null");
                }
                //System.out.println((String)iter.next());
            }//while iter
            System.out.println(users.length);
*/

        
        
        }


        
    }//main

