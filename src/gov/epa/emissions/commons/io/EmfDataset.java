package gov.epa.emissions.commons.io;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//TODO: how are these unused variables accessed ?
public class EmfDataset implements Dataset {

    private int year;

    private String description;

    private Map dataTables;

    private String datasetType;

    private String region;

    private String country;

    private String units;

    private String creator;

    private String temporalResolution;

    private Date startDateTime;

    private Date endDateTime;

    private Map datasources;

    public EmfDataset() {
        dataTables = new HashMap();
    }

    public String getDatasetType() {
        return datasetType;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public void setTemporalResolution(String temporalResolution) {
        this.temporalResolution = temporalResolution;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setStartDateTime(Date time) {
        this.startDateTime = time;
    }

    public void setStopDateTime(Date time) {
        this.endDateTime = time;
    }

    public String getRegion() {
        return region;
    }

    public int getYear() {
        return year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDataTable(String tableType) {
        return (String) ((dataTables != null) ? dataTables.get(tableType) : null);
    }

    public Map getDataTables() {
        return dataTables;
    }

    public void setDatasetType(String datasetType) {
        this.datasetType = datasetType;
    }

    public void addDataTable(String tableType, String tableName) {
        dataTables.put(tableType, tableName);
    }

    public void setDataSources(Map dataSources) {
        this.datasources = dataSources;//table type -> filepath mapping
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getUnits() {
        return units;
    }

    public void setDataTables(Map datatablesMap) {
        this.dataTables = datatablesMap;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreator() {
        return creator;
    }

    public String getTemporalResolution() {
        return temporalResolution;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public Date getStopDateTime() {
        return endDateTime;
    }

    public Map getDataSources() {
        return datasources;
    }

}
