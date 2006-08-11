package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.ui.RowSource;

public class InputsRowSource implements RowSource {

    private CaseInput input;

    public InputsRowSource(CaseInput source) {
        this.input = source;
    }

    public Object[] values() {
        return new Object[] { new Long(input.getId()), input.getName()};
    }

    public Object source() {
        return input;
    }

    public void validate(int rowNumber) {// No Op
    }

    public void setValueAt(int column, Object val) {// No Op
    }
}