/*
 * Creation on Nov 17, 2005
 * Eclipse Project Name: EMF
 * File Name: DTVTRecord.java
 * Author: Conrad F. D'Cruz
 */

package gov.epa.emissions.framework.version;

public class DTVTRecord {

    private int recordId;
    private int datasetId;
    private int versionNumber;
    private String description;
    private String versionName;
    
    public DTVTRecord() {
        super();
    }

    public DTVTRecord(int recid, int dsid, String description, String versName, int versNum) {
        super();
        datasetId = dsid;
        this.description = description;
        recordId = recid;
        versionName = versName;
        versionNumber = versNum;
    }

    /**
     * @param id
     * @param description
     * @param name
     * @param number
     */
    public DTVTRecord(int dsid, String description, String versName, int versNum) {
        super();
        datasetId = dsid;
        this.description = description;
        versionName = versName;
        versionNumber = versNum;
    }

    /**
     * @return Returns the datasetId.
     */
    public int getDatasetId() {
        return datasetId;
    }

    /**
     * @param datasetId The datasetId to set.
     */
    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns the recordId.
     */
    public int getRecordId() {
        return recordId;
    }

    /**
     * @param recordId The recordId to set.
     */
    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    /**
     * @return Returns the versionName.
     */
    public String getVersionName() {
        return versionName;
    }

    /**
     * @param versionName The versionName to set.
     */
    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    /**
     * @return Returns the versionNumber.
     */
    public int getVersionNumber() {
        return versionNumber;
    }

    /**
     * @param versionNumber The versionNumber to set.
     */
    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    
}
