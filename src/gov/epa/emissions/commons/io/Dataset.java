/*
 * Creation on Aug 29, 2005
 * Eclipse Project Name: EMF
 * File Name: Dataset.java
 * Author: Conrad F. D'Cruz
 */
/**
 * 
 */

package gov.epa.emissions.commons.io;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public interface Dataset extends Serializable {
    // unique id needed for hibernate persistence
    public long getDatasetid();

    public void setDatasetid(long datasetid);

    public String getName();

    public void setName(String name);

    // bean-style properties
    void setCreator(String creator);//TODO: use User instead

    String getCreator();

    String getDatasetType();

    void setDatasetType(String datasetType);

    void setUnits(String units);

    String getUnits();

    void setRegion(String region);

    String getRegion();

    void setYear(int year);

    int getYear();

    String getCountry();

    void setCountry(String country);

    void setTemporalResolution(String name);

    String getTemporalResolution();

    void setStartDateTime(Date time);

    Date getStartDateTime();

    void setStopDateTime(Date time);

    Date getStopDateTime();

    String getDescription();

    void setDescription(String description);

    Map getTablesMap();

    void setTablesMap(Map datatables);

    void setDataSources(Map dataSources);

    Map getDataSources();

    // convenience methods
    Table getTable(String tableType);

    void addTable(Table importedTable);

    public Table[] getTables();
}
