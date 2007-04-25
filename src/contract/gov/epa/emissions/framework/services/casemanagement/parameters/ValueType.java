package gov.epa.emissions.framework.services.casemanagement.parameters;

public class ValueType {

    private int id;
    
    private String name;
    
    public ValueType() {
        //
    }
    
    public ValueType(String name) {
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
    
    public String toString() {
        return this.name;
    }
}
