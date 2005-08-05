package gov.epa.emissions.framework;

import gov.epa.emissions.commons.io.Dataset;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class EmfDataset implements Dataset {

    public String getDatasetType() {
        return null;
    }

    public String getDataTable(String tableType) {
        return null;
    }

    public void setDataSources(Map dataSources) {
    }

    public void setDatasetType(String datasetType) {
    }

    public void addDataTable(String tableType, String tableName) {
    }

    public void setUnits(List units) {
    }

    public void setTemporalResolution(String name) {
    }

    public void setRegion(String countryName) {
    }

    public void setYear(int year) {
    }

    public void setStartDateTime(Date time) {
    }

    public void setStopDateTime(Date time) {
    }

    public String getRegion() {
        return null;
    }

    public String getYear() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public void setDescription(String description) {
    }

    public Map getDataTables() {
        return null;
    }

}
