package gov.epa.emissions.framework.services.casemanagement;

public class AirQualityModel implements Comparable {

    private int id;

    private String name;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public AirQualityModel() {
        super();
    }

    public AirQualityModel(String name) {
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
        if (other == null || !(other instanceof AirQualityModel))
            return false;

        return ((AirQualityModel) other).name.equals(this.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(Object other) {
        return name.compareTo(((AirQualityModel) other).getName());
    }
}
