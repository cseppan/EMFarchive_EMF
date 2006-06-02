package gov.epa.emissions.framework.services.cost.data;

import java.io.Serializable;

public class CostRecord implements Serializable {

    private String name, pollutant;
    
    private int id, costYear;
    
    private float costPerTon;

    public int getCostYear() {
        return costYear;
    }

    public void setCostYear(int costYear) {
        this.costYear = costYear;
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

    public String getPollutant() {
        return pollutant;
    }

    public void setPollutant(String pollutant) {
        this.pollutant = pollutant;
    }

    public float getCostPerTon() {
        return costPerTon;
    }

    public void setCostPerTon(float costPerTon) {
        this.costPerTon = costPerTon;
    }
    
}
