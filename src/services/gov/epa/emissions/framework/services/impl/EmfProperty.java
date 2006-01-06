package gov.epa.emissions.framework.services.impl;

import java.io.Serializable;

public class EmfProperty implements Serializable {

    private long id;

    private String name = null;

    private String value = null;

    public EmfProperty() {// needed for persistence
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
