package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.security.User;

import java.util.Date;

/**
 * This class keeps track of the date/time a user initiated an export of a particular version of a dataset to a
 * repository (location).
 * 
 * @author Conrad F. D'Cruz
 * 
 */
public class Revision {

    private int id;

    private int datasetId;

    private int version;

    private User creator;

    private Date date;

    private String what;

    private String why;

    private String references;

    public Revision() {// No argument constructor needed for hibernate mapping
    }

    public Revision(User creator, int datasetId, Date date, int version, String what, String why, String references) {
        this.creator = creator;
        this.datasetId = datasetId;
        this.date = date;
        this.version = version;
        this.what = what;
        this.why = why;
        this.references = references;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public int getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getWhat() {
        return what;
    }

    public void setWhat(String what) {
        this.what = what;
    }

    public String getWhy() {
        return why;
    }

    public void setWhy(String why) {
        this.why = why;
    }

    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
    }

}
