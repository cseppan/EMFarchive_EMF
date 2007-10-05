package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.ui.RowSource;

public class ParametersRowSource implements RowSource {

    private CaseParameter parameter;
    
    private EmfSession session;

    public ParametersRowSource(CaseParameter source, EmfSession session) {
        this.parameter = source;
        this.session = session;
    }

    public Object[] values() {
        return new Object[] { getParameterName(parameter), getOrder(parameter),   
                getEnvtVarName(parameter), getSectorName(parameter), getJob(parameter),
                getProgramName(parameter), parameter.getValue(),
                getValueType(parameter), isRequired(parameter), isShow(parameter), 
                parameter.getNotes(), parameter.getPurpose()};  
    }
    
    private Float getOrder(CaseParameter parameter) {
        return new Float(parameter.getOrder());
    }

    private String getValueType(CaseParameter parameter) {
        return (parameter.getType() == null) ? "" : parameter.getType().getName();
    }

    private String getParameterName(CaseParameter param) {
        return (param.getParameterName() == null) ? "" : param.getParameterName().getName();
    }
    
    private String getSectorName(CaseParameter param) {
        return (param.getSector() == null) ? "All sectors" : param.getSector().getName();
    }
    
    private String getProgramName(CaseParameter param) {
        return (param.getProgram() == null) ? "" : param.getProgram().getName();
    }
    
    private String getEnvtVarName(CaseParameter param) {
        return (param.getEnvVar() == null) ? "" : param.getEnvVar().getName();
    }
    
    private String isRequired(CaseParameter param) {
        return (param == null) ? "" : param.isRequired() + "";
    }
    
    private String isShow(CaseParameter input) {
        return (input == null) ? "" : input.isShow() + "";
    }

    private String getJob(CaseParameter param) {
        try {
            CaseJob job = session.caseService().getCaseJob(param.getJobId());
            return (job == null) ? ParameterFieldsPanelPresenter.ALL_FOR_SECTOR : job.getName();
        } catch (EmfException e) {
            return null;
        }
    }
    
    public Object source() {
        return parameter;
    }

    public void validate(int rowNumber) {// No Op
    }

    public void setValueAt(int column, Object val) {// No Op
    }
}