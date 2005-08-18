/*
 * Created on Jul 28, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.commons;
 * File Name: EMFStatus.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.services;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class Status implements Serializable{

    private String statusid=null;
    private String userName;
    private String msgType;
    private String message;
    private boolean msgRead=false;
    private Date timestamp = null;

    /**
     * @return Returns the statusid.
     */
    public String getStatusid() {
        return statusid;
    }
    /**
     * @param statusid The statusid to set.
     */
    public void setStatusid(String statusid) {
        this.statusid = statusid;
    }

    /**
     * @return Returns the msgRead.
     */
    public boolean isMsgRead() {
        return msgRead;
    }

    /**
     * @param msgRead The msgRead to reset.
     */
    public void setMsgRead() {
        this.msgRead=true;
    }

    /**
     * @param msgRead The msgRead to set.
     */
    public void setMsgRead(boolean msgRead) {
        this.msgRead = msgRead;
    }

    /**
     * The default constructor 
     */
    public Status() {
        super();
    }

    /**
     * The constructor that takes in the status message parameters
     * 
     * @param userName
     * @param msgType
     * @param message
     * @param timestamp
     */
    public Status(String userName, String msgType, String message,
            Date timestamp) {
        super();
        this.userName = userName;
        this.msgType = msgType;
        this.message = message;
        this.timestamp = timestamp;
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
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return Returns the message.
     */
    public String getMessage() {
        return message;
    }
    /**
     * @param message The message to set.
     */
    public void setMessage(String message) {
        this.message = message;
    }
    /**
     * @return Returns the msgType.
     */
    public String getMsgType() {
        return msgType;
    }
    /**
     * @param msgType The msgType to set.
     */
    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }
    /**
     * @return Returns the timestamp.
     */
    public Date getTimestamp() {
        return timestamp;
    }
    /**
     * @param timestamp The timestamp to set.
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String toString() {
        return "Message : " + message + " for user: " + userName;
    }
}
