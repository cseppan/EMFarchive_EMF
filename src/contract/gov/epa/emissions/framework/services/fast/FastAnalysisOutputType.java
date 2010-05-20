package gov.epa.emissions.framework.services.fast;

import java.io.Serializable;

public class FastAnalysisOutputType implements Serializable {

    private int id;

    private String name;

    public static final String DETAILED_SECTOR_MAPPING_RESULT = "Detailed Sector Mapping Result";

    public static final String DETAILED_EECS_MAPPING_RESULT = "Detailed EECS Mapping Result";

    public static final String SECTOR_SPECIFIC_INVENTORY = "Sector Specific Inventory";

    public FastAnalysisOutputType() {
        //
    }

    public FastAnalysisOutputType(String name) {
        this();
        this.name = name;
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

    public String toString() {
        return getName();
    }
    
    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof FastAnalysisOutputType && ((FastAnalysisOutputType) other).id == id);
    }
}
