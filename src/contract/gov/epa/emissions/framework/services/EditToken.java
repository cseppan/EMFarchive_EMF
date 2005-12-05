package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.version.Version;

public class EditToken {

    private String table;

    private int version;

    private long datasetId;

    public EditToken() {// needed by Axis
    }

    public EditToken(Version version, String table) {
        this.datasetId = version.getDatasetId();
        this.version = version.getVersion();
        this.table = table;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public int getVersion() {
        return version;
    }

    public long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(long datasetId) {
        this.datasetId = datasetId;
    }

    public void setVersion(int version) {
        this.version = version;
    }

}
