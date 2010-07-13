package gov.epa.emissions.framework.services.fast;

import java.io.Serializable;

public class FastRunOutputType implements Serializable, Comparable {

    private int id;

    private String name;

    public static final String annotatedInventoryWithEECS = "Annotated Inventory with EECS";

    public static final String DETAILED_SECTOR_MAPPING_RESULT = "Detailed Sector Mapping Result";

    public static final String DETAILED_EECS_MAPPING_RESULT = "Detailed EECS Mapping Result";

    public static final String SECTOR_SPECIFIC_INVENTORY = "Sector Specific Inventory";

    public static final String GRIDDED_EMISSIONS = "Gridded Emissions";

    public static final String GRIDDED_SUMMARY_EMISSIONS_AIR_QUALITY = "Gridded Summary Emissions and Air Quality";

    public static final String GRIDDED_DETAILED_EMISSIONS_AIR_QUALITY = "Gridded Detailed Emissions and Air Quality";

    public FastRunOutputType() {
        //
    }

    public FastRunOutputType(String name) {
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

    public int compareTo(Object o) {
        return name.compareTo(((FastRunOutputType) o).getName());
    }
    
    public String toString() {
        return getName();
    }
    
    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof FastRunOutputType && ((FastRunOutputType) other).id == id);
    }
}
