package gov.epa.emissions.framework.services;

import java.io.Serializable;

public class Country implements Serializable {

    private long id;

    private String name;

    public Country() {// needed for serialization
    }

    public Country(String name) {
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

}
