package gov.epa.emissions.framework.services;

import java.io.Serializable;

public class IntendedUse implements Serializable, Comparable {

    private long id;

    private String name;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public IntendedUse() {
        super();
    }

    public IntendedUse(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String toString(){
        return getName();
    }

    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof IntendedUse))
            return false;

        final IntendedUse iu = (IntendedUse) other;

        if (!(iu.getName().equals(this.getName())))
            return false;

        return true;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public int compareTo(Object o) {
        return name.compareTo(((IntendedUse)o).getName());
    }

}
