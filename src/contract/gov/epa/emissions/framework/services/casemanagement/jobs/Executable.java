package gov.epa.emissions.framework.services.casemanagement.jobs;

import java.io.Serializable;

public class Executable implements Serializable {

    private int id;
    
    private String name;
    
    private String description;

    public Executable() {
        //
    }
    
    public Executable(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
    
    public String toString() {
        return this.name;
    }
    
}
