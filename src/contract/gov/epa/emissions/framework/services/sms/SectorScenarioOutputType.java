package gov.epa.emissions.framework.services.sms;

import java.io.Serializable;

public class SectorScenarioOutputType implements Serializable, Comparable {

    private int id;

    private String name;

    public static final String annotatedInventoryWithEECS = "Annotated Inventory with EECS";

    public static final String detailedSectorMapping = "Detailed Sector Mapping Result";

    public static final String detailedEECSMapping = "Detailed EECS Mapping Result";

    public static final String sectorSpecificInventory = "Sector Specific Inventory";

    public SectorScenarioOutputType() {
        //
    }

    public SectorScenarioOutputType(String name) {
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
        return name.compareTo(((SectorScenarioOutputType) o).getName());
    }
    
    public String toString() {
        return getName();
    }
    
    public int hashCode() {
        return name.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof SectorScenarioOutputType && ((SectorScenarioOutputType) other).id == id);
    }
}
