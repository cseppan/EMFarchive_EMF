package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.db.version.Version;

import java.io.Serializable;

public class GeoRegion implements Serializable, Comparable<GeoRegion> {

    private int id;

    private String name;
    
    private RegionType type;
    
    private String abbreviation;
    
    private String resolution;
    
    private String ioapiName;
    
    private String mapProjection;
    
    private String description;
    
    private float xorig, yorig, xcell, ycell;
    
    private int ncols, nrows, nthik;
    
    private int datasetId;
    
    private Version version;
    
    public int getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getIoapiName() {
        return ioapiName;
    }

    public void setIoapiName(String ioapiName) {
        this.ioapiName = ioapiName;
    }

    public String getMapProjection() {
        return mapProjection;
    }

    public void setMapProjection(String mapProjection) {
        this.mapProjection = mapProjection;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getXorig() {
        return xorig;
    }

    public void setXorig(float xorig) {
        this.xorig = xorig;
    }

    public float getYorig() {
        return yorig;
    }

    public void setYorig(float yorig) {
        this.yorig = yorig;
    }

    public float getXcell() {
        return xcell;
    }

    public void setXcell(float xcell) {
        this.xcell = xcell;
    }

    public float getYcell() {
        return ycell;
    }

    public void setYcell(float ycell) {
        this.ycell = ycell;
    }

    public int getNcols() {
        return ncols;
    }

    public void setNcols(int ncols) {
        this.ncols = ncols;
    }

    public int getNrows() {
        return nrows;
    }

    public void setNrows(int nrows) {
        this.nrows = nrows;
    }

    public int getNthik() {
        return nthik;
    }

    public void setNthik(int nthik) {
        this.nthik = nthik;
    }

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public GeoRegion() {
        super();
    }

    public GeoRegion(String name) {
        this.name = name;
    }
    
    public GeoRegion(String name, String desc) {
        this.name = name;
        this.description = desc;
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

    public boolean equals(Object other) {
        if (other == null || !(other instanceof GeoRegion))
            return false;

        return ((GeoRegion) other).name.equals(this.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(GeoRegion other) {
        if (other == null)
            return -1;
        
        return name.compareToIgnoreCase(other.getName());
    }

    public RegionType getType() {
        return type;
    }

    public void setType(RegionType type) {
        this.type = type;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }
}
