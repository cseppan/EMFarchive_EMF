package gov.epa.emissions.framework.services;

import java.io.Serializable;

public class IntendedUse implements Serializable {

    private long id;
    private String name;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public IntendedUse() {
        super();
    }

    public IntendedUse(String name) {
        this.name=name;
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

}
