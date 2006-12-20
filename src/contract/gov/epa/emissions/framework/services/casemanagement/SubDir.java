package gov.epa.emissions.framework.services.casemanagement;

import java.io.Serializable;

public class SubDir implements Serializable, Comparable {
    
    private int id;

    private String name;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public SubDir() {
        super();
    }

    public SubDir(String name) {
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
        if (other == null || !(other instanceof SubDir))
            return false;

        return (id == ((SubDir)other).id) || ((SubDir) other).name.equals(this.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(Object other) {
        return name.compareTo(((SubDir) other).getName());
    }

}
