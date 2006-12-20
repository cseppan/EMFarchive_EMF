package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.ui.RowSource;

public class InputsRowSource implements RowSource {

    private CaseInput input;

    public InputsRowSource(CaseInput source) {
        this.input = source;
    }

    public Object[] values() {
        return new Object[] { getInputName(input), getSectorName(input), getProgramName(input),
                getEnvtVarName(input), getDatasetName(input), getVersion(input), getQAStatus(input),
                getDSType(input), isRequired(input), isShow(input), getSubDir(input) };
    }
    
    private String getInputName(CaseInput input) {
        return (input.getInputName() == null) ? "" : input.getInputName().getName();
    }
    
    private String getSectorName(CaseInput input) {
        return (input.getSector() == null) ? "All sectors" : input.getSector().getName();
    }
    
    private String getProgramName(CaseInput input) {
        return (input.getProgram() == null) ? "" : input.getProgram().getName();
    }
    
    private String getEnvtVarName(CaseInput input) {
        return (input.getEnvtVars() == null) ? "" : input.getEnvtVars().getName();
    }
    
    private String getDatasetName(CaseInput input) {
        return (input.getDataset() == null) ? "" : input.getDataset().getName();
    }
    
    private String getVersion(CaseInput input) {
        return (input.getVersion() == null) ? "" : input.getVersion().getVersion() + "";
    }
    
    private String getQAStatus(CaseInput input) {
        return "";
    }
    
    private String getDSType(CaseInput input) {
        return (input.getDatasetType() == null) ? "" : input.getDatasetType().getName();
    }
    
    private String isRequired(CaseInput input) {
        return (input == null) ? "" : input.isRequired() + "";
    }
    
    private String isShow(CaseInput input) {
        return (input == null) ? "" : input.isShow() + "";
    }

    private String getSubDir(CaseInput input) {
        return (input == null) ? "" : input.getSubdirObj().toString();
    }
    
    public Object source() {
        return input;
    }

    public void validate(int rowNumber) {// No Op
    }

    public void setValueAt(int column, Object val) {// No Op
    }
}