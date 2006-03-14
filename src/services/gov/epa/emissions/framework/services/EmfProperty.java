package gov.epa.emissions.framework.services;

import java.io.Serializable;

public class EmfProperty implements Serializable {

    private int id;

    private String name;

    private String value;

    public EmfProperty() {// needed for persistence
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
