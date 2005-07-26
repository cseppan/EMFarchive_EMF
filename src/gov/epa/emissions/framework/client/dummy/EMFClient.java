/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFClient
 * Package: package gov.epa.emissions.framework.service.axis;
 * File Name: EMFClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.client.dummy;
  
import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.EMFUserAdminTransport;
import gov.epa.emissions.framework.commons.EMFUserAdmin;
import gov.epa.emissions.framework.commons.User;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class EMFClient {
    private static String endpoint = 
        "http://ben.cep.unc.edu:8080/emf/services/EMFUserManagerService";

    /**
     * 
     */
    public EMFClient() {
        super();
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) throws EmfException {
        String statuscode = null;
        
        EMFUserAdmin emfUserAdmin = new EMFUserAdminTransport(endpoint);
        System.out.println("IN EMFCLIENT main");
       
        String uname = "cdcruz";
        String pwd = "conrad12345";
        
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

        user.setFullName("Peter Rabbit 222");
        
        User[] users = new User[1];
        users[0]= user;
        
        emfUserAdmin.updateUsers(users);
        
        //emfUserAdmin.updateUser(user);
//        User[] users = emfUserAdmin.getUsers();
//        if (users == null){
//            System.out.println("Call returned a null USERS list");
//        }else{
//            for (int i=0; i<users.length; i++){
//                User aUser = users[i];
//                System.out.println("user #" + i + "************************");
//                System.out.println(aUser.getUserName());
//                System.out.println(aUser.getPassword());
//                System.out.println(aUser.getFullName());
//                System.out.println(aUser.getEmailAddr());
//                System.out.println(aUser.getAffiliation());
//                System.out.println(aUser.getWorkPhone());
//                System.out.println("Admin? " + aUser.isInAdminGroup());
//                System.out.println("Disabled? " + aUser.isAcctDisabled());
//            }
//            System.out.println("****************************");
//
//        }
//       

//        user = emfUserAdmin.getUser(uname);
//        System.out.println("current password= " + user.getPassword());
//        String status = null;
//        uname = "fflintstone2";
//        pwd = "freddie";
//        if (user != null){
//            user.setUserName(uname);
//            user.setPassword(pwd);
//            emfUserAdmin.createUser(user);            
//        }else{
//            System.out.println("encountered null user object");
//        }
//        System.out.println("status of create= " + status);
// 
//        user.setUserName("fflintstone");
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

        /*
        List users = emfUserAdmin.getUsers();
        if (users == null){
            System.out.println("List of Users was NULL");
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
            
        }


 */   
       
//        User[] users = emfUserAdmin.getUsers();
//        if (users == null){
//            System.out.println("Call returned a null USERS list");
//        }else{
//
//            Iterator iter = users.iterator();
//            
//            while (iter.hasNext()){
//                User aUser = (User) iter.next();
//                if (aUser !=null){
//                    System.out.println(aUser.getFullName());                    
//                }else{
//                    System.out.println("User was null");
//                }
//                //System.out.println((String)iter.next());
//            }//while iter
//            System.out.println(users.length);


        
        
        }


        
    }//main

