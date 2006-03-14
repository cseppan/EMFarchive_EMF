package gov.epa.emissions.framework.services.data;


/**
 * This class keeps track of the date/time a user initiated an export of a particular version of a dataset to a
 * repository (location).
 * 
 * @author Conrad F. D'Cruz
 * 
 */
public class NoteType {

    private long id;

    private String type = null;

    public NoteType() {// No argument constructor needed for hibernate mapping
    }

    public NoteType(String type) {
        super();
        setType(type);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
