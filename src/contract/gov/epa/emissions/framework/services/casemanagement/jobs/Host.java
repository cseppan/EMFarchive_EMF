package gov.epa.emissions.framework.services.casemanagement.jobs;

import java.io.Serializable;

public class Host implements Serializable {

    private int id;
    
    private String name;
    
    private String ipAddress;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
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
