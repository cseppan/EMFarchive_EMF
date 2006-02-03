package gov.epa.emissions.framework.services;

import java.util.Date;

/**
 * This class keeps track of the date/time a user initiated an export of a
 * particular version of a dataset to a repository (location).
 * 
 * @author Conrad F. D'Cruz
 * 
 */
public class AccessLog {

    private long id;

    private long datasetId;

    private String username = null;

    private Date timestamp = null;

    private String version = "v1";

    private String description = null;

    private String folderPath = null;

    public AccessLog() {// No argument constructor needed for hibernate mapping
    }

    public String toString() {
        return "[ " + id + " " + datasetId + " " + username + " " + version + " ]";
    }

    public AccessLog(String username, long datasetid, Date date, String version, String description, String folderPath) {
        super();
        setUsername(username);
        setDatasetId(datasetid);
        setTimestamp(date);
        setVersion(version);
        setDescription(description);
        setFolderPath(folderPath);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(long datasetid) {
        this.datasetId = datasetid;
    }

}
