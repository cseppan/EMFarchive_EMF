/*
 * Created on Jun 28, 2005
 *
 * Eclipse Project Name: EMFServer
 * Package: package gov.epa.emissions.framework.dummy;
 * File Name: UserAdmin.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.dummy;

import gov.epa.emissions.framework.UserException;
import gov.epa.emissions.framework.commons.User;

import java.util.Hashtable;

/**
 * @author Conrad F. D'Cruz
 *  
 */
public class UserAdminDummy {

    Hashtable allUsers = null;

    /**
     * @throws UserException 
     *  
     */
    public UserAdminDummy() throws UserException {
        super();
        allUsers = new Hashtable();

        User u1 = new User("Robert", "UNC", "966-1234", "bob@unc.edu", "Bob",
                "bob234", true, false);
        User u2 = new User("Joseph", "UNC", "966-2345", "joe@unc.edu", "Joe",
                "joe234", true, false);
        User u3 = new User("Suzanne", "UNC", "966-3456", "suzy@unc.edu", "Suzy",
                "suzy234", false, false);
        User u4 = new User("Conrad F. D'Cruz", "UNC", "843-8593", "cdcruz@email.unc.edu", "cdcruz",
                "conrad123", true, false);

        User u5 = new User("Roger Rabbit", "UNC", "966-8593", "rabbit@fred.unc.edu", "rrabbit",
                "roger123", true, true);

        User u6 = new User("Neil Armstrong", "UNC", "966-9111", "neil@fred.unc.edu", "narmstrong",
                "neil123", false, true);
        
        allUsers.put("Bob", u1);
        allUsers.put("Joe", u2);
        allUsers.put("Suzy", u3);
        allUsers.put("cdcruz", u4);
        allUsers.put("rrabbit", u5);
        allUsers.put("narmstrong", u6);
        
    }

    /**
     * 
     * @param userName
     * @param pwd
     * @param wantAdminStatus
     * @return
     */
    public String authenticate(String userName, String pwd,
            boolean wantAdminStatus) {

        String returnCode = null;
        
        if (allUsers.containsKey(userName)){
            User aUser = (User) allUsers.get(userName);
            
            if (aUser.getPassword().equals(pwd)){
                if (!wantAdminStatus){
                    returnCode="Valid";
                }else{
                    if (aUser.isInAdminGroup()){
                        returnCode="Valid";
                    }else{
                        returnCode="Cant Be Admin";
                    }
                }
            }else{
                returnCode="Incorrect Password";
            }
            
        }else{
          returnCode="Incorrect User Name";    
        }
        
        return returnCode;
    }

}
