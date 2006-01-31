package gov.epa.emissions.framework.ui;

import java.util.List;

public interface TableData {

    String[] columns();

    Class getColumnClass(int col);

    List rows();

    boolean isEditable(int col);

    Object element(int row);

    List elements(int[] selected);

    void setValueAt(Object value, int row, int col);
    
}
