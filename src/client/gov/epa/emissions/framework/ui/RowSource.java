package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.EmfException;

public interface RowSource {

    Object[] values();

    void setValueAt(int column, Object val);

    Object source();

    void validate(int rowNumber) throws EmfException;

}
