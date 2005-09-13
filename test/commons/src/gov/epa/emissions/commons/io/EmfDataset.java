/*
 * Creation on Aug 29, 2005
 * Eclipse Project Name: EMF
 * File Name: EmfDataset.java
 * Author: Conrad F. D'Cruz
 */
/**
 * 
 */

package gov.epa.emissions.commons.io;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Conrad F. D'Cruz
 * 
 */
public class EmfDataset implements Dataset {
    // TODO: how are these unused variables accessed ?

    // unique id needed for hibernate persistence
    private long datasetid;

    private String name;

    private int year;

    private String description;

    private String datasetType;

    private String region;

    private String country="US";

    private String units;

    private String creator;

    private String temporalResolution;

    private Date startDateTime;

    private Date endDateTime;

    private Map datasources;

    private List tables;

    public EmfDataset() {
        tables = new ArrayList();
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

    public Table getTable(String tableType) {
        for (Iterator iter = tables.iterator(); iter.hasNext();) {
            Table element = (Table) iter.next();
            if (element.getTableType().equals(tableType))
                return element;
        }

        return null;
    }

    // TODO: return a list. Also, change the Hibernate mapping
    public Map getTablesMap() {
        Map tablesMap = new HashMap();

        for (Iterator iter = tables.iterator(); iter.hasNext();) {
            Table element = (Table) iter.next();
            tablesMap.put(element.getTableType(), element.getTableName());
        }

        return tablesMap;
    }

    public void setDatasetType(String datasetType) {
        this.datasetType = datasetType;
    }

    public void addTable(Table table) {
        tables.add(table);
    }

    public void setDataSources(Map dataSources) {
        this.datasources = dataSources;// table type -> filepath mapping
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

    public void setTablesMap(Map tablesMap) {
        tables.clear();

        for (Iterator iter = tablesMap.keySet().iterator(); iter.hasNext();) {
            String tableType = (String) iter.next();
            tables.add(new Table(tableType, (String) tablesMap.get(tableType)));
        }
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

    public long getDatasetid() {
        return datasetid;
    }

    public void setDatasetid(long datasetid) {
        this.datasetid = datasetid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Table[] getTables() {
        return (Table[]) tables.toArray(new Table[0]);
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof Dataset)) {
            return false;
        }

        Dataset otherDataset = (Dataset) other;

        return (name.equals(otherDataset.getName()));
    }
}
