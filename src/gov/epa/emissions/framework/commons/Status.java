/*
 * Created on Jul 28, 2005
 *
 * Eclipse Project Name: EMF
 * Package: package gov.epa.emissions.framework.commons;
 * File Name: EMFStatus.java
 * Author: Conrad F. D'Cruz
 */
package gov.epa.emissions.framework.commons;

import java.util.Date;

/**
 * @author Conrad F. D'Cruz
 *
 */
public class Status {

    private String userName;
    private String msgType;
    private String message;
    private Date timestamp = null;

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
}
