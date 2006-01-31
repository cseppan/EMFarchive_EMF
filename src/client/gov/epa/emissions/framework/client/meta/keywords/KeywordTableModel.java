package gov.epa.emissions.framework.client.meta.keywords;

import gov.epa.emissions.framework.ui.EmfTableModel;

public class KeywordTableModel extends EmfTableModel {

    public KeywordTableModel(EditableKeyValueTableData tableData) {
        super(tableData);
    }
    
    public boolean isCellEditable(int row, int col) {
        return ((EditableKeyValueTableData)tableData).isEditable(row, col);
    }

}
