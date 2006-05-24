package gov.epa.emissions.framework.client.cost.controlmeasure;

public class Scc {

    private String code;

    private String description;

    public Scc(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }
    
}
