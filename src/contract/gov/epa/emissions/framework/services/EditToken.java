package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.version.Version;

public class EditToken {

    private String table;

    private Version version;

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

}
