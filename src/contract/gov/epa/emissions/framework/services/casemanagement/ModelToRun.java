package gov.epa.emissions.framework.services.casemanagement;

public class ModelToRun {
    private int id;

    private String name;

    /*
     * Default constructor needed for hibernate and axis serialization
     */
    public ModelToRun() {
        super();
    }

    public ModelToRun(String name) {
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
        if (other == null || !(other instanceof ModelToRun))
            return false;

        return ((ModelToRun) other).name.equals(this.name) || ((ModelToRun)other).id == id;
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return getName();
    }

    public int compareTo(Object other) {
        return name.compareTo(((ModelToRun) other).getName());
    }
}
