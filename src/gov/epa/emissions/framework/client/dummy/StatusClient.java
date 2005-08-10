/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFClient
 * Package: package gov.epa.emissions.framework.service.axis;
 * File Name: EMFClient.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.client.dummy;
  
import java.util.Date;

import gov.epa.emissions.framework.EmfException;
import gov.epa.emissions.framework.client.transport.StatusServicesTransport;
import gov.epa.emissions.framework.client.transport.UserServicesTransport;
import gov.epa.emissions.framework.commons.StatusServices;
import gov.epa.emissions.framework.commons.UserServices;
import gov.epa.emissions.framework.commons.Status;
import gov.epa.emissions.framework.commons.User;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class StatusClient {
    private static String endpoint1 = 
        "http://ben.cep.unc.edu:8080/emf/services/EMFUserManagerService";
    private static String endpoint2 = 
    "http://ben.cep.unc.edu:8080/emf/services/gov.epa.emf.StatusServices";
        
    public StatusClient() throws EmfException{
        super();
        callServiceForGet();
        //callServiceForInsert();
    }
    public static void main(String[] args) {
        try {
            new StatusClient();
        } catch (EmfException e) {
            e.printStackTrace();
        }
        
//        EMFUserAdmin emfUserAdmin = new EMFUserAdminTransport(endpoint1);
//        EMFStatus emfStatus = new EMFStatusTransport(endpoint2);
//
//        System.out.println("IN EMFCLIENT main");
//       
//        String uname = "cdcruz";
//        String pwd = "conrad12345";
//  
//        try{
//            Status[] allStats = emfStatus.getMessages(uname);
//            System.out.println("Array of status messages size= " + allStats.length);
//            Status aStat = allStats[0];
//            System.out.println("name: " + aStat.getUserName());
//            System.out.println("" + aStat.getMsgType());
//            System.out.println("" + aStat.getMessage());
//            System.out.println("" + aStat.getTimestamp());
//            Status aStat = new Status();
//            aStat.setMessage("From the Dummy Client");
//            aStat.setMsgType("DUMMY");
//            aStat.setTimestamp(new Date());
//            aStat.setUserName("cdcruz");
//            emfStatus.setStatus(aStat);
//            
//        }catch(EmfException ex){
//            ex.printStackTrace();
//        }
//        try {
//            emfUserAdmin.authenticate(uname, pwd, true);
//            emfUserAdmin.authenticate(uname, pwd, false);        
//            User user = emfUserAdmin.getUser(uname);
//            if (user == null){
//                System.out.println("User object is null");
//            }else{
//                System.out.println("getUser succeeded: " + user.getFullName());
//                
//            }
//        } catch (EmfException ex) {
//              //e.printStackTrace();
//            System.out.println(ex.getMessage());
//        }        
//        System.out.println(uname + " login status is: " + statuscode);


//        user.setFullName("Peter Rabbit 222");
//        
//        User[] users = new User[1];
//        users[0]= user;
//        
//        emfUserAdmin.updateUsers(users);
        
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


    /**
     * @throws EmfException
     * 
     */
    private void callServiceForInsert() throws EmfException {
        Status aStat = new Status();
        aStat.setMessage("import started for file XYZABC");
        aStat.setMsgType("INFOMATICA");
        aStat.setTimestamp(new Date());
        aStat.setUserName("cdcruz");
      StatusServices emfStatusSvc= new StatusServicesTransport(endpoint2);

        System.out.println("HibClient: Before call to setStatus");
        emfStatusSvc.setStatus(aStat);
        System.out.println("HibClient: After call to setStatus");

    }

    /**
     * 
     */
    private void callServiceForGet() {
        StatusServices emfStatusSvc = new StatusServicesTransport(endpoint2);
        try {
            Status[] stats = emfStatusSvc.getMessages("ejones");
            System.out.println("Total number of status messages: " + stats.length);
            
            for (int i=0; i<stats.length; i++){
                Status aStat = stats[i];
                //System.out.println("" + aStat.getStatusid());
                System.out.println("" + aStat.getUserName());
                System.out.println("" + aStat.getTimestamp());
                System.out.println("" + aStat.getMsgType());
                System.out.println("" + aStat.getMessage());
                System.out.println("" + aStat.isMsgRead());
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
    }

