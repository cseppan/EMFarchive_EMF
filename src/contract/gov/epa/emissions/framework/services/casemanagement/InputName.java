package gov.epa.emissions.framework.services.casemanagement;

import java.io.Serializable;

public class InputName implements Serializable, Comparable {

    private int id;

    private String name;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public InputName() {
        super();
    }

    public InputName(String name) {
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
        if (other == null || !(other instanceof InputName))
            return false;

        return ((InputName) other).name.equals(this.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(Object other) {
        return name.compareTo(((InputName) other).getName());
    }
}
