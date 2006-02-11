package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.security.User;

import java.util.Date;

/**
 * This class keeps track of the date/time a user initiated an export of a particular version of a dataset to a
 * repository (location).
 * 
 * @author Conrad F. D'Cruz
 * 
 */
public class Note {

    private long id;
    private long datasetId;
    private long version;
    private String name;
    private User creator;
    private Date date;
    private NoteType noteType;
    private String details;
    private String references;
    
    public Note() {// No argument constructor needed for hibernate mapping
    }

    public String toString(){
       String delim=":";
       String output = "[" + id + delim + datasetId + delim + version + delim + name + delim 
                           + creator + delim + date + delim + noteType + delim + details + delim
                           + references + "]";
      return output;   
    }
    
    public Note(User creator, long datasetId, Date date, String details, String name, NoteType type, String references, long version) {
        super();
        // TODO Auto-generated constructor stub
        this.creator = creator;
        this.datasetId = datasetId;
        this.date = date;
        this.details = details;
        this.name = name;
        noteType = type;
        this.references = references;
        this.version = version;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(long datasetId) {
        this.datasetId = datasetId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NoteType getNoteType() {
        return noteType;
    }

    public void setNoteType(NoteType noteType) {
        this.noteType = noteType;
    }

    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }



}
