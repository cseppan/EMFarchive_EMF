package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.security.User;

import java.util.Date;

public class Note {

    private int id;

    private int datasetId;

    private int version;

    private String name;

    private User creator;

    private Date date;

    private NoteType noteType;

    private String details;

    private String references;

    public Note() {// No argument constructor needed for hibernate mapping
    }

    public String toString() {
        return name + " (" + id + ")";
    }

    public Note(User creator, int datasetId, Date date, String details, String name, NoteType type, String references,
            int version) {
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

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

}
