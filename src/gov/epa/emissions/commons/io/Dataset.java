package gov.epa.emissions.commons.io;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Dataset {

    String getDatasetType();

    String getDataTable(String tableType);

    void setDataSources(Map dataSources);

    void setDatasetType(String datasetType);

    void addDataTable(String tableType, String tableName);

    void setUnits(List units);

    void setTemporalResolution(String name);

    void setRegion(String countryName);

    void setYear(int year);

    void setStartDateTime(Date time);

    void setStopDateTime(Date time);

    String getRegion();

    String getYear();

    String getDescription();

    void setDescription(String description);

    Map getDataTables();

}
