package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.EditableTableModel;

public class EditableEmfTableModel extends EmfTableModel implements EditableTableModel {

    public EditableEmfTableModel(TableData tableData) {
        super(tableData);
    }

}
