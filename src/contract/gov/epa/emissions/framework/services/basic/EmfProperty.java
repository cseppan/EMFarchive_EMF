package gov.epa.emissions.framework.services.basic;

import java.io.Serializable;

public class EmfProperty implements Serializable {

    private int id;

    private String name;

    private String value;

    public static String POSTGIS_COUNTY_FIELDS = "POSTGIS_COUNTY_FIELDS";
    public static String POSTGIS_STATE_FIELDS = "POSTGIS_STATE_FIELDS";
    public static String POSTGIS_LATITUDE_FIELDS = "POSTGIS_LATITUDE_FIELDS";
    public static String POSTGIS_LONGITUDE_FIELDS = "POSTGIS_LONGITUDE_FIELDS";
    public static String POSTGIS_POINT_LATLON_FIELDS = "POSTGIS_POINT_LATLON_FIELDS";
    public static final String DOWNLOAD_EXPORT_FOLDER_PROPERTY = "DOWNLOAD_EXPORT_FOLDER";
    public static final String DOWNLOAD_EXPORT_ROOT_URL_PROPERTY = "DOWNLOAD_EXPORT_ROOT_URL";


    public EmfProperty() {// needed for persistence
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
