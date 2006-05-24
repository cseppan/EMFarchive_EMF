package gov.epa.emissions.framework.services.cost.data;

import java.io.Serializable;

public class EfficiencyRecord implements Serializable {

    private String name;
    
    private int id;
    
    private String pollutant;
    
    private float efficiency;

    public float getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(float efficiency) {
        this.efficiency = efficiency;
    }

    public String getPollutant() {
        return pollutant;
    }

    public void setPollutant(String pollutant) {
        this.pollutant = pollutant;
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
    
}
