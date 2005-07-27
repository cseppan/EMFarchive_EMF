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
 * The User value object encapsulates all of the EMF user data The User object
 * is serialized between the server and client using Apache Axis Web Services
 * (SOAP/HTTP and XML)
 * 
 */
public class User implements Serializable {

    // State variables for the User bean
    private String name;

    private String affiliation;

    private String phone;

    private String email;

    private String userName;

    private String password;

    private boolean inAdminGroup = false;

    private boolean acctDisabled = false;

    // dirty flag to indicate some value in the bean has changed
    private boolean dirty = false;

    public boolean equals(Object other) {
        if (!(other instanceof User))
            return false;

        User otherUser = (User) other;
        return this.userName.equals(otherUser.userName);
    }

    public int hashCode() {
        return userName.hashCode();
    }

    public User() {
        super();
    }

    public User(String name, String affiliation, String phone, String email, String username, String password,
            boolean beAdmin, boolean disabled) throws UserException {

        setFullName(name);
        setAffiliation(affiliation);
        setWorkPhone(phone);
        setEmailAddr(email);
        setUserName(username);
        setPassword(password);

        this.inAdminGroup = beAdmin;
        this.acctDisabled = disabled;
        this.dirty = true;
    }

    /**
     * @return Returns the acctDisabled.
     */
    public boolean isAcctDisabled() {
        return acctDisabled;
    }

    public void setAcctDisabled(boolean acctDisabled) {
        this.acctDisabled = acctDisabled;
        this.dirty = true;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) throws UserException {
        if (affiliation == null)
            throw new UserException("Affiliation should be specified");

        if (affiliation.length() < 3) {
            throw new UserException("Affiliation should have 2 or more characters");
        }

        this.affiliation = affiliation;
        this.dirty = true;
    }

    public String getEmailAddr() {
        return email;
    }

    public void setEmailAddr(String email) throws UserException {
        if (email == null)
            throw new UserException("Email should be specified");

        if (!Pattern.matches("^([a-zA-Z]+)(\\w)*@(\\w)+.(\\w)+(.\\w+)*", email))
            throw new UserException("Email should have the format xx@yy.zz");

        this.email = email;
        this.dirty = true;
    }

    public String getFullName() {
        return name;
    }

    public void setFullName(String name) throws UserException {
        if (name == null)
            throw new UserException("Name should be specified");
        this.name = name;
        this.dirty = true;
    }

    public boolean isInAdminGroup() {
        return inAdminGroup;
    }

    public void setInAdminGroup(boolean inAdminGroup) {
        this.inAdminGroup = inAdminGroup;
        this.dirty = true;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) throws UserException {
        if (password == null)
            throw new UserException("Password should be specified");

        if (password.length() < 8) {
            throw new UserException("Password should have at least 8 characters");
        }

        // password should start w/ an alphabet, contain atleast one digit,
        // and only contains digits or alphabets
        if (!Pattern.matches("^([a-zA-Z]+)(\\d+)(\\w)*", password)) {
            throw new UserException("One or more characters of password should be a non-letter");
        }

        if (password.equals(userName)) {
            throw new UserException("Username should be different from Password");
        }

        this.password = password;
        this.dirty = true;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String username) throws UserException {
        if (username == null) {
            throw new UserException("Username should be specified");
        }
        if (username.length() < 3) {
            throw new UserException("Username should have at least 3 characters");
        }

        if (username.equals(password)) {
            throw new UserException("Username should be different from Password");
        }

        this.userName = username;
        this.dirty = true;
    }

    public String getWorkPhone() {
        return phone;
    }

    public void setWorkPhone(String phone) throws UserException {
        if (phone == null)
            throw new UserException("Phone should be specified");

        if (!Pattern.matches("(\\d)+(-\\d+)*", phone)) {
            throw new UserException("Phone should have format xxx-yyy-zzzz or xxxx or x-yyyy");
        }

        this.phone = phone;
        this.dirty = true;

    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void confirmPassword(String confirmPassword) throws UserException {
        if (!password.equals(confirmPassword)) {
            throw new UserException("Confirm Password does not match Password");
        }

    }
}
