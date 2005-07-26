/*
 * Created on Jun 27, 2005
 *
 * Eclipse Project Name: EMFCommons
 * Package: package gov.epa.emissions.framework.commons;
 * File Name: User.java
 * Author: Conrad F. D'Cruz
 * 
 */
package gov.epa.emissions.framework.commons;

import gov.epa.emissions.framework.UserException;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * @author Conrad F. D'Cruz
 *
 * The User value object encapsulates all of the EMF user data
 * The User object is serialized between the server and client using 
 * Apache Axis Web Services (SOAP/HTTP and XML)
 * 
 */
public class User implements Serializable {

    //State variables for the User bean
    private String name;
    private String affiliation;
    private String phone;
    private String email;
    private String userName;
    private String password;
    private boolean inAdminGroup = false;
    private boolean acctDisabled = true;

    //  dirty flag to indicate some value in the bean has changed
    private boolean dirty = false;  
    
    /**
     *
     */

    public boolean equals(Object other) {
        if(!(other instanceof User)) return false;
        
        User otherUser = (User)other;
        return this.userName.equals(otherUser.userName);
    }
    /**
     *
     */

    public int hashCode() {
        return userName.hashCode();
    }
    /**
     * The no argument constructor is needed to comply to the
     * Java Beans specification. 
     * 
     */
    public User() {
        super();        
    }

    /**
     * 
     * @param name
     * @param affiliation
     * @param phone
     * @param email
     * @param username
     * @param password
     * @param beAdmin
     * @param diabled
     * @throws UserException 
     */
    public User(String name, String affiliation, String phone, String email, String username, String password, boolean beAdmin, boolean disabled) throws UserException{
    
        this.name=name;
        setAffiliation(affiliation);
        setWorkPhone(phone);
        setEmailAddr(email);
        setUserName(username);
        setPassword(password);
        this.inAdminGroup=beAdmin;
        this.acctDisabled=disabled;
        this.dirty=true;
    }
    
    /**
     * @return Returns the acctDisabled.
     */
    public boolean isAcctDisabled() {
        return acctDisabled;
    }
    
    /**
     * @param acctDisabled The acctDisabled to set.
     */
    public void setAcctDisabled(boolean acctDisabled) {
        this.acctDisabled = acctDisabled;
        this.dirty = true;
    }

    /**
     * @return Returns the affiliation.
     */
    public String getAffiliation() {
        return affiliation;
    }
    
    /**
     * @param affiliation The affiliation to set.
     */
    public void setAffiliation(String affiliation) throws UserException {
        if(affiliation.length() < 3) {
            throw new UserException("Affiliation should have 2 or more characters");
        }
        
        this.affiliation = affiliation;
        this.dirty = true;
    }
    
    /**
     * @return Returns the emailAddr.
     */
    public String getEmailAddr() {
        return email;
    }
    
    /**
     * @param emailAddr The emailAddr to set.
     * @throws UserException 
     */
    public void setEmailAddr(String emailAddr) throws UserException {
        if(!Pattern.matches("^([a-zA-Z]+)(\\w)*@(\\w)+.(\\w)+(.\\w+)*", emailAddr)) {
            throw new UserException("Email should have 2 or more characters");
        }
        
        this.email = emailAddr;
        this.dirty = true;
    }
    
    /**
     * @return Returns the fullName.
     */
    public String getFullName() {
        return name;
    }
    
    /**
     * @param fullName The fullName to set.
     */
    public void setFullName(String fullName) {
        this.name = fullName;
        this.dirty = true;
    }
    
    /**
     * @return Returns the inAdminGroup.
     */
    public boolean isInAdminGroup() {
        return inAdminGroup;
    }
    
    /**
     * @param inAdminGroup The inAdminGroup to set.
     */
    public void setInAdminGroup(boolean inAdminGroup) {
        this.inAdminGroup = inAdminGroup;
        this.dirty = true;
    }
    
    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * @param password The password to set.
     */
    public void setPassword(String password) throws UserException {
        if(password.length() < 8) {
            throw new UserException("Password should have at least 8 characters");
        }
        
        // password should start w/ an alphabet, contain atleast one digit, 
        // and only contains digits or alphabets
        if(!Pattern.matches("^([a-zA-Z]+)(\\d+)(\\w)*", password)) {             
            throw new UserException("One or more characters of password should be a non-letter");
        }
        
        if(password.equals(userName)) {
            throw new UserException("Username should be different from Password");
        }
        
        this.password = password;
        this.dirty = true;
    }
    
    /**
     * @return Returns the userName.
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * @param username The userName to set.
     */
    public void setUserName(String username) throws UserException {
        if(username == null) {
            throw new UserException("Username should be specified");
        }
        if(username.length() < 3) {
            throw new UserException("Username should have at least 3 characters");
        }
        
        if(username.equals(password)) {
            throw new UserException("Username should be different from Password");
        }
        
        this.userName = username;
        this.dirty = true;
    }
    
    /**
     * @return Returns the workPhone.
     */
    public String getWorkPhone() {
        return phone;
    }
    
    /**
     * @param phone The workPhone to set.
     */
    public void setWorkPhone(String phone) throws UserException {
        if(phone == null) 
            throw new UserException("Phone should be specified");
        
        if(!Pattern.matches("(\\d)+(-\\d+)*", phone)) {
            throw new UserException("Phone should have format xxx-yyy-zzzz or xxxx or x-yyyy");
        }
        
        this.phone = phone;
        this.dirty = true;

    }
    
    /**
     * @return Returns the dirty.
     */
    public boolean isDirty() {
        return dirty;
    }
    
    /**
     * @param dirty The dirty to set.
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
    
    public void confirmPassword(String confirmPassword) throws UserException {
        if(!password.equals(confirmPassword)) {
            throw new UserException("Password does not match Confirm Password");
        }
            
    }
}
