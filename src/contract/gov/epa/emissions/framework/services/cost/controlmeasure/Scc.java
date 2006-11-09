package gov.epa.emissions.framework.services.cost.controlmeasure;

public class Scc {

    private int id;

    private int controlMeasureId;

    private String code;

    private String description;

    private String status;

    public Scc() {
        // Empty
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getControlMeasureId() {
        return controlMeasureId;
    }

    public void setControlMeasureId(int controlMeasureId) {
        this.controlMeasureId = controlMeasureId;
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

    // scc id field is not compared=> id initial values are zero so two new sccs with different codes will be equal
    // before persisting
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Scc))
            return false;
        Scc other = (Scc) obj;
        return code.equals(other.getCode()) && controlMeasureId == other.getControlMeasureId();
    }

    public int hashCode() {
        return code.hashCode();
    }

}
