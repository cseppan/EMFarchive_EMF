package gov.epa.emissions.framework.services.data;

import java.util.Date;

import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.security.User;

public class QAStep {

    private String name;

    private String program;

    private int version;

    private String programArguments;

    private boolean required;

    private float order;

    private String status;

    private String result;

    private User who;

    private Date when;

    private int datasetId;

    private int id;

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
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
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

    public void setWhen(Date when) {
        this.when = when;
    }

    public void setWho(User who) {
        this.who = who;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public String getStatus() {
        return status;
    }

    public Date getWhen() {
        return when;
    }

    public User getWho() {
        return who;
    }

    public void setDatasetId(int datasetId) {
        this.datasetId = datasetId;
    }

    public int getDatasetId() {
        return datasetId;
    }

}
