package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.db.version.Version;

import java.io.Serializable;
import java.util.Date;

public class QAStep implements Serializable {

    private String name;

    private QAProgram program;

    private int version;

    private String programArguments;

    private boolean required;

    private float order;

    private String status;

    private String comments;

    private String who;

    private Date date;

    private int datasetId;

    private int id;

    private String description;

    private String configuration;

    private String outputFolder;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public QAStep() {// needed as it's a Java bean
    }

    public QAStep(QAStepTemplate template, int version) {
        this.name = template.getName();
        this.version = version;
        this.program = template.getProgram();
        this.programArguments = template.getProgramArguments();
        this.required = template.isRequired();
        this.order = template.getOrder();
        this.description = template.getDescription();
    }

    public QAStep(EmfDataset dataset, Version version, QAStepTemplate template) {
        this.name = template.getName();
        this.datasetId = dataset.getId();
        this.version = version.getVersion();
        this.program = template.getProgram();
        this.programArguments = template.getProgramArguments();
        this.required = template.isRequired();
        this.order = template.getOrder();
        this.description = template.getDescription();
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof QAStep))
            return false;

        QAStep step = (QAStep) obj;
        if (id == step.id
                || (name.equals(step.getName()) && version == step.getVersion() && datasetId == step.getDatasetId()))
            return true;

        return false;
    }

    public QAProgram getProgram() {
        return program;
    }

    public void setProgram(QAProgram program) {
        this.program = program;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public float getOrder() {
        return order;
    }

    public void setOrder(float order) {
        this.order = order;
    }

    public String getProgramArguments() {
        return programArguments;
    }

    public void setProgramArguments(String programArguments) {
        this.programArguments = programArguments;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setWho(String who) {
        this.who = who;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComments() {
        return comments;
    }

    public String getStatus() {
        return status;
    }

    public Date getDate() {
        return date;
    }

    public String getWho() {
        return who;
    }

    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    public int getDatasetId() {
        return datasetId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String toString() {
        return name;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

}
