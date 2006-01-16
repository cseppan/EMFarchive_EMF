package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;

public class EditToken {

    private String table;

    private Version version;

    private User user;

    public EditToken() {// needed by Axis
    }

    public EditToken(Version version, String table) {
        this.version = version;
        this.table = table;
    }

    public String getTable() {
        return table;
    }

    public Version getVersion() {
        return version;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Object key() {
        return "DatasetId:" + version.getDatasetId() + "-Version:" + version.getVersion() + "-Table:" + getTable();
    }

    public long datasetId() {
        return version.getDatasetId();
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public boolean isLocked(User user) {
        return version.isLocked(user);
    }

}
