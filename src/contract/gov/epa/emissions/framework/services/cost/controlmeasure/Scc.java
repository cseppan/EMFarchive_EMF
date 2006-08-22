package gov.epa.emissions.framework.services.cost.controlmeasure;

public class Scc {

    private String code;

    private String description;

    private String status;

    public Scc() {
        // Empty
    }

    public Scc(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Scc))
            return false;
        Scc other = (Scc) obj;
        return code.equals(other.getCode());
    }

    public int hashCode() {
        return code.hashCode();
    }

}
