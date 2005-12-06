package gov.epa.emissions.framework.services;

import java.io.Serializable;
import java.util.Date;

public class Status implements Serializable {

    private long statusid;

    private String username;

    private String messageType;

    private String message;

    private boolean msgRead = false;

    private Date timestamp = null;

    public boolean isMsgRead() {
        return msgRead;
    }

    public void setMsgRead() {
        this.msgRead = true;
    }

    public void setMsgRead(boolean msgRead) {
        this.msgRead = msgRead;
    }

    public Status() {// needed for serialization
    }

    public Status(String username, String msgType, String message, Date timestamp) {
        super();
        this.username = username;
        this.messageType = msgType;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Date getTimestamp() {
        return timestamp;
    }

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
