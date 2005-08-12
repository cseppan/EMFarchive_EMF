package gov.epa.emissions.commons.io.importer;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Dataset {

    String getDatasetType();

    void setUnits(List units);

    void setTemporalResolution(String name);

    void setRegion(String countryName);

    void setYear(int year);

    void setStartDateTime(Date time);

    void setStopDateTime(Date time);

    String getRegion();

    int getYear();

    String getDescription();

    void setDescription(String description);

    Object getDataTable(String tableType);

    Map getDataTables();

    void setDatasetType(String datasetType);

    void addDataTable(String tableType, String tableName);

    void setDataSources(Map dataSources);
}
