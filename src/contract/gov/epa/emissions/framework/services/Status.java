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

    private long statusid;
    private String username;
    private String messageType;
    private String message;
    private boolean msgRead=false;
    private Date timestamp = null;

    
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
     * @param username
     * @param msgType
     * @param message
     * @param timestamp
     */
    public Status(String username, String msgType, String message,
            Date timestamp) {
        super();
        this.username = username;
        this.messageType = msgType;
        this.message = message;
        this.timestamp = timestamp;
    }

    /**
     * @return Returns the username.
     */
    public String getUsername() {
        return username;
    }
    /**
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
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
    public String getMessageType() {
        return messageType;
    }
    /**
     * @param messageType The msgType to set.
     */
    public void setMessageType(String messageType) {
        this.messageType = messageType;
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
        return "Message : " + message + " for user: " + username;
    }

	public long getStatusid() {
		return statusid;
	}

	public void setStatusid(long statusid) {
		this.statusid = statusid;
	}
}
