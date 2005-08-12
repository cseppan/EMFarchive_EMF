package gov.epa.emissions.commons.io;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: how are these unused variables accessed ?
public class EmfDataset implements Dataset {

    private int year;

    private String description;

    private Map dataTables;

    private String datasetType;

    private String region;

    public EmfDataset() {
        dataTables = new HashMap();
    }

    public String getDatasetType() {
        return datasetType;
    }

    public void setUnits(List units) {
    }

    public void setTemporalResolution(String temporalResolution) {
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setStartDateTime(Date time) {
    }

    public void setStopDateTime(Date time) {
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

    public Object getDataTable(String tableType) {
        return (dataTables != null) ? dataTables.get(tableType) : null;
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
    }

}
