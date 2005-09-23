package gov.epa.emissions.framework.ui;

import java.util.List;

public interface EmfTableData {

    String[] columns();

    List rows();

    boolean isEditable(int col);

    Object element(int row);

    List elements(int[] selected);

}
