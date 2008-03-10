package gov.epa.emissions.framework.services.casemanagement;

import java.io.Serializable;

public class Grid implements Serializable, Comparable<Grid> {

    private int id;

    private String name;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public Grid() {
        super();
    }

    public Grid(String name) {
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

    public boolean equals(Object other) {
        if (other == null || !(other instanceof Grid))
            return false;

        return ((Grid) other).name.equals(this.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(Grid other) {
        return name.compareToIgnoreCase(other.getName());
    }
}
