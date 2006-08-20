package gov.epa.emissions.framework.services.casemanagement;

import java.io.Serializable;

public class Program implements Serializable, Comparable {

    private int id;

    private String name;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public Program() {
        super();
    }

    public Program(String name) {
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
        if (other == null || !(other instanceof Program))
            return false;

        return ((Program) other).name.equals(this.name) || ((Program)other).id == id;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(Object other) {
        return name.compareTo(((Program) other).getName());
    }
}
