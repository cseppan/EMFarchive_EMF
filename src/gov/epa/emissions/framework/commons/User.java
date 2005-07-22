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
    private String fullName;
    private String affiliation;
    private String workPhone;
    private String emailAddr;
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
     * @param affil
     * @param workPhone
     * @param emailAddr
     * @param uname
     * @param pwd
     * @param beAdmin
     * @param diabled
     */
    public User(String name, String affil, String wkPhone, String emailAddr, String uname, String pwd, boolean beAdmin, boolean disabled){
    
        this.fullName=name;
        this.affiliation=affil;
        this.workPhone=wkPhone;
        this.emailAddr=emailAddr;
        this.userName=uname;
        this.password=pwd;
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
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
        this.dirty = true;
    }
    
    /**
     * @return Returns the emailAddr.
     */
    public String getEmailAddr() {
        return emailAddr;
    }
    
    /**
     * @param emailAddr The emailAddr to set.
     */
    public void setEmailAddr(String emailAddr) {
        this.emailAddr = emailAddr;
        this.dirty = true;
    }
    
    /**
     * @return Returns the fullName.
     */
    public String getFullName() {
        return fullName;
    }
    
    /**
     * @param fullName The fullName to set.
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
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
    public void setPassword(String password) {
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
     * @param userName The userName to set.
     */
    public void setUserName(String userName) throws UserException {
        if(userName.length() < 3) {
            throw new UserException("Username must have at least 3 characters");
        }
        
        this.userName = userName;
        this.dirty = true;
    }
    
    /**
     * @return Returns the workPhone.
     */
    public String getWorkPhone() {
        return workPhone;
    }
    
    /**
     * @param workPhone The workPhone to set.
     */
    public void setWorkPhone(String workPhone) {
        this.workPhone = workPhone;
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
}
