package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.ui.RowSource;

public class OutputsRowSource implements RowSource {

    public OutputsRowSource(CaseInput source) {
        //
    }

    public Object[] values() {
        return new Object[] { "", "", "", "", "", "", "", "", "", "" };
    }

    public void setValueAt(int column, Object val) {
        // NOTE Auto-generated method stub
        
    }

    public Object source() {
        // NOTE Auto-generated method stub
        return null;
    }

    public void validate(int rowNumber) throws EmfException {
        // NOTE Auto-generated method stub
        throw new EmfException("Under construction.");
    }
}
    
